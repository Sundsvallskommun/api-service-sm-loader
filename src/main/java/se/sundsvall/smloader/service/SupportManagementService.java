package se.sundsvall.smloader.service;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.LOCATION;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseMapping;

import generated.se.sundsvall.supportmanagement.Errand;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
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
	private static final String SLACK_MESSAGE = "SmLoader failed to export cases: ";
	private static final String SLACK_ATTACHMENT_MESSAGE = "SmLoader failed to export attachments: ";
	private static final String EMAIL_MESSAGE = "Failed to export cases: ";
	private static final String EMAIL_ATTACHMENT_MESSAGE = "Failed to export attachments: ";
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

	public void exportCases(final String municipalityId) {
		RequestId.init();
		final var failedCases = new ArrayList<String>();
		final var failedAttachments = new HashMap<String, List<String>>();

		final var casesToExport = caseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);

		casesToExport.forEach(caseEntity -> {
			final var mapper = openEMapperMap.get(caseEntity.getCaseMetaData().getFamilyId());
			if (mapper == null) {
				caseRepository.save(caseEntity.withDeliveryStatus(FAILED));
				LOGGER.error("No mapper found for familyId: {}", caseEntity.getCaseMetaData().getFamilyId());
				return;
			}

			final var decodedOpenECase = Base64.getDecoder().decode(Optional.ofNullable(caseEntity.getOpenECase()).orElse(""));
			final var errand = mapper.mapToErrand(decodedOpenECase);
			final var errandId = sendToSupportManagement(errand, caseEntity.getCaseMetaData().getNamespace(), caseEntity.getCaseMetaData().getMunicipalityId());
			if (errandId == null) {
				caseRepository.save(caseEntity.withDeliveryStatus(FAILED));
				failedCases.add(caseEntity.getExternalCaseId());
				return;
			}
			final var caseMapping = toCaseMapping(errandId, caseEntity);
			caseMappingRepository.save(caseMapping);
			caseRepository.save(caseEntity.withDeliveryStatus(CREATED));

			openEService.updateOpenECaseStatus(caseEntity.getExternalCaseId(), caseEntity.getCaseMetaData());

			final var createdErrand = getErrandFromSupportManagement(errandId, caseEntity.getCaseMetaData().getNamespace(), caseEntity.getCaseMetaData().getMunicipalityId());

			final var faultyAttachments = attachmentService.handleAttachments(decodedOpenECase, caseEntity, errandId);
			if (!faultyAttachments.isEmpty()) {
				caseRepository.save(caseEntity.withDeliveryStatus(FAILED));
				failedAttachments.put(errandId, faultyAttachments);
			}
			openEService.confirmDelivery(caseEntity.getExternalCaseId(), caseEntity.getCaseMetaData().getInstance(), Optional.ofNullable(createdErrand).map(Errand::getErrandNumber).orElse(null));
		});

		handleFailedCases(municipalityId, failedCases);
		handleFailedAttachments(municipalityId, failedAttachments);
	}

	private String sendToSupportManagement(final Errand errand, final String namespace, final String municipalityId) {
		try {
			final var result = supportManagementClient.createErrand(municipalityId, namespace, errand);
			final var location = String.valueOf(result.getHeaders().getFirst(LOCATION));
			return location.substring(location.lastIndexOf("/") + 1);
		} catch (final Exception e) {
			LOGGER.error("Failed to send errand to SupportManagement", e);
			return null;
		}
	}

	private Errand getErrandFromSupportManagement(final String errandId, final String namespace, final String municipalityId) {
		try {
			return supportManagementClient.getErrand(municipalityId, namespace, errandId);
		} catch (final Exception e) {
			LOGGER.error("Failed to get errand from SupportManagement", e);
			return null;
		}
	}

	private void handleFailedCases(final String municipalityId, final List<String> failedCases) {
		if (!failedCases.isEmpty()) {
			LOGGER.error("Failed to export cases: {}", failedCases);
			final var subject = List.of(environment.getActiveProfiles()).contains(PRODUCTION) ? PROD_SUBJECT : TEST_SUBJECT;
			messagingClient.sendSlack(municipalityId, messagingMapper.toRequest(SLACK_MESSAGE + failedCases));
			messagingClient.sendEmail(municipalityId, messagingMapper.toEmailRequest(subject, EMAIL_MESSAGE + failedCases));
		}
	}

	private void handleFailedAttachments(final String municipalityId, final Map<String, List<String>> failedAttachments) {
		if (!failedAttachments.isEmpty()) {
			final var subject = List.of(environment.getActiveProfiles()).contains(PRODUCTION) ? PROD_SUBJECT : TEST_SUBJECT;
			final var slackMessage = new StringBuilder(SLACK_ATTACHMENT_MESSAGE);
			final var emailMessage = new StringBuilder(EMAIL_ATTACHMENT_MESSAGE);

			failedAttachments.forEach((caseId, attachments) -> {
				slackMessage.append(String.format("For case %s the attachments %s were not exported.", caseId, attachments));
				emailMessage.append(String.format("For case %s the attachments %s were not exported.", caseId, attachments));
			});

			LOGGER.error("Failed to export attachments: {}", failedAttachments);
			messagingClient.sendSlack(municipalityId, messagingMapper.toRequest(slackMessage.toString()));
			messagingClient.sendEmail(municipalityId, messagingMapper.toEmailRequest(subject, emailMessage.toString()));
		}
	}
}
