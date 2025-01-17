package se.sundsvall.smloader.service;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.LOCATION;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseMapping;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMappingId;
import se.sundsvall.smloader.integration.messaging.MessagingClient;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.MessagingMapper;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

@Service
public class SupportManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupportManagementService.class);
	private static final String PRODUCTION = "production";
	private static final String PROD_SUBJECT = "SmLoader - Production";
	private static final String TEST_SUBJECT = "SmLoader - Test";
	private static final String MESSAGE = "SmLoader failed to export cases: ";
	private final SupportManagementClient supportManagementClient;
	private final CaseRepository caseRepository;
	private final CaseMappingRepository caseMappingRepository;
	private final Map<String, OpenEMapper> openEMapperMap;
	private final OpenEService openEService;
	private final MessagingClient messagingClient;
	private final MessagingMapper messagingMapper;
	private final Environment environment;
	private final AttachmentService attachmentService;

	public SupportManagementService(final SupportManagementClient supportManagementClient, final CaseRepository caseRepository, final CaseMappingRepository caseMappingRepository, final List<OpenEMapper> openEMappers,
		final OpenEService openEService, final MessagingClient messagingClient, final MessagingMapper messagingMapper, final Environment environment, final AttachmentService attachmentService) {
		this.supportManagementClient = supportManagementClient;
		this.caseRepository = caseRepository;
		this.caseMappingRepository = caseMappingRepository;
		this.openEMapperMap = openEMappers.stream().collect(toMap(OpenEMapper::getSupportedFamilyId, Function.identity()));
		this.openEService = openEService;
		this.messagingClient = messagingClient;
		this.messagingMapper = messagingMapper;
		this.environment = environment;
		this.attachmentService = attachmentService;
	}

	public void exportCases(final String municipalityId, Consumer<String> exportHealthConsumer) {
		RequestId.init();
		final var failedCases = new ArrayList<String>();
		final var failedAttachments = new HashMap<String, List<String>>();

		final var casesToExport = caseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		final var onlyFailedCases = caseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING).isEmpty();

		// Loop over all cases
		casesToExport.forEach(caseEntity -> {
			final var openEMapper = Optional.ofNullable(openEMapperMap.get(caseEntity.getCaseMetaData().getFamilyId()));
			if (openEMapper.isEmpty()) {
				LOGGER.error("No mapper found for familyId: {}", caseEntity.getCaseMetaData().getFamilyId());
			}

			// Each method can be called multiple times for the same caseEntity without causing duplication/error.
			// If export fails caseEntity is marked as FAILED and retry will occur next time job is run.
			// If all is successful caseEntity is marked as CREATED.
			openEMapper
				.map(mapper -> mapper.mapToErrand(Base64.getDecoder().decode(caseEntity.getOpenECase())))
				.flatMap(errand -> sendToSupportManagement(errand, caseEntity.getCaseMetaData().getNamespace(), caseEntity.getCaseMetaData().getMunicipalityId()))
				.flatMap(errandId -> saveCaseMapping(errandId, caseEntity))
				.flatMap(errandId -> exportAttachments(errandId, caseEntity, failedAttachments))
				.flatMap(errandId -> updateOpenEStatus(errandId, caseEntity))
				.flatMap(errandId -> confirmDelivery(errandId, caseEntity))
				.ifPresentOrElse(
					errandId -> caseRepository.save(caseEntity.withDeliveryStatus(CREATED)),
					saveFailed(caseEntity, failedCases, exportHealthConsumer));
		});

		// Avoid reporting when run only consist of previously failed (reported) cases to minimize spam
		if (!onlyFailedCases) {
			reportFailedCases(municipalityId, failedCases, failedAttachments);
		}
	}

	private Runnable saveFailed(CaseEntity caseEntity, List<String> failedCases, Consumer<String> exportHealthConsumer) {
		return () -> {
			caseRepository.save(caseEntity.withDeliveryStatus(FAILED));
			failedCases.add(caseEntity.getExternalCaseId());
			exportHealthConsumer.accept("Failed to export " + failedCases.size() + " errands!");
		};
	}

	private Optional<String> updateOpenEStatus(String errandId, CaseEntity caseEntity) {
		if (openEService.updateOpenECaseStatus(caseEntity.getExternalCaseId(), caseEntity.getCaseMetaData())) {
			return Optional.of(errandId);
		} else {
			return Optional.empty();
		}
	}

	private Optional<String> confirmDelivery(String errandId, CaseEntity caseEntity) {
		final var confirmSuccessful = getErrandFromSupportManagement(errandId, caseEntity.getCaseMetaData().getNamespace(), caseEntity.getCaseMetaData().getMunicipalityId())
			.map(Errand::getErrandNumber)
			.map(errandNr -> openEService.confirmDelivery(caseEntity.getExternalCaseId(), caseEntity.getCaseMetaData().getInstance(), errandNr))
			.orElse(false);

		return confirmSuccessful ? Optional.of(errandId) : Optional.empty();
	}

	private Optional<String> exportAttachments(String errandId, CaseEntity caseEntity, HashMap<String, List<String>> failedAttachments) {
		final var faultyAttachments = attachmentService.handleAttachments(Base64.getDecoder().decode(caseEntity.getOpenECase()), caseEntity, errandId);
		if (!faultyAttachments.isEmpty()) {
			failedAttachments.put(errandId, faultyAttachments);
			return Optional.empty();
		}
		return Optional.of(errandId);
	}

	private Optional<String> saveCaseMapping(String errandId, CaseEntity caseEntity) {
		// Case can already be saved if case creation was successful but attachment export failed.
		if (!caseMappingRepository.existsById(CaseMappingId.create().withErrandId(errandId).withExternalCaseId(caseEntity.getExternalCaseId()))) {
			caseMappingRepository.save(toCaseMapping(errandId, caseEntity));
		}
		return Optional.of(errandId);
	}

	private Optional<String> sendToSupportManagement(final Errand errand, final String namespace, final String municipalityId) {
		try {
			final var filter = String.join(" and ", errand.getExternalTags().stream()
				.sorted(Comparator.comparing(ExternalTag::getKey))
				.map(this::createFilterForTag)
				.toList())
				.concat(String.format(" and channel:'%s'", errand.getChannel()));

			// Check if errand is already exported, if not create it.
			return supportManagementClient.findErrands(municipalityId, namespace, filter).stream()
				.findFirst()
				.map(Errand::getId)
				.or(() -> {
					final var result = supportManagementClient.createErrand(municipalityId, namespace, errand);
					final var location = String.valueOf(result.getHeaders().getFirst(LOCATION));
					return Optional.of(location.substring(location.lastIndexOf("/") + 1));
				});
		} catch (final Exception e) {
			LOGGER.error("Failed to send errand to SupportManagement", e);
			return Optional.empty();
		}
	}

	private String createFilterForTag(ExternalTag tag) {
		return String.format("exists(externalTags.key:'%s' and externalTags.value:'%s')", tag.getKey(), tag.getValue());
	}

	private Optional<Errand> getErrandFromSupportManagement(final String errandId, final String namespace, final String municipalityId) {
		try {
			return Optional.of(supportManagementClient.getErrand(municipalityId, namespace, errandId));
		} catch (final Exception e) {
			LOGGER.error("Failed to get errand from SupportManagement", e);
			return Optional.empty();
		}
	}

	private void reportFailedCases(final String municipalityId, final List<String> failedCases, final Map<String, List<String>> failedAttachments) {
		if (!failedCases.isEmpty()) {
			LOGGER.error("Failed to export cases: {}", failedCases);
			final var subject = List.of(environment.getActiveProfiles()).contains(PRODUCTION) ? PROD_SUBJECT : TEST_SUBJECT;
			StringBuilder attachmentMessage = new StringBuilder(".\n");
			if (!failedAttachments.isEmpty()) {
				failedAttachments.forEach((caseId, attachments) -> {
					attachmentMessage.append(String.format(String.format("For case %s the attachments %s were not exported.%n", caseId, attachments)));
				});
			}
			var message = MESSAGE
				.concat(failedCases.toString())
				.concat(attachmentMessage.toString())
				.concat(String.format("RequestId: %s", RequestId.get()));

			messagingClient.sendSlack(municipalityId, messagingMapper.toRequest(message));
			messagingClient.sendEmail(municipalityId, messagingMapper.toEmailRequest(subject, message));
		}
	}
}
