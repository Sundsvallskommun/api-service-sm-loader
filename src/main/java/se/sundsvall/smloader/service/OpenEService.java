package se.sundsvall.smloader.service;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.SYSTEM_SUPPORT_MANAGEMENT;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseEntity;

import feign.Response;
import generated.se.sundsvall.oepintegrator.CaseEnvelope;
import generated.se.sundsvall.oepintegrator.CaseStatusChangeRequest;
import generated.se.sundsvall.oepintegrator.ConfirmDeliveryRequest;
import generated.se.sundsvall.oepintegrator.InstanceType;
import generated.se.sundsvall.oepintegrator.ModelCase;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;
import se.sundsvall.smloader.integration.oepintegrator.OepIntegratorClient;

@Service
public class OpenEService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenEService.class);

	private final OepIntegratorClient oepIntegratorClient;
	private final CaseRepository caseRepository;
	private final CaseMetaDataRepository caseMetaDataRepository;

	public OpenEService(final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository, final CaseMetaDataRepository caseMetaDataRepository) {
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.caseMetaDataRepository = caseMetaDataRepository;
	}

	public void fetchAndSaveNewOpenECases(final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId, final Consumer<String> importHealthConsumer) {
		RequestId.init();
		final var effectiveToDate = nonNull(toDate) ? toDate : LocalDateTime.now();

		if (fromDate.isAfter(effectiveToDate)) {
			LOGGER.error("From-date: '{}' is after to-date: '{}'. No cases will be fetched.", fromDate, effectiveToDate);
			importHealthConsumer.accept(String.format("From-date: '%s' is after to-date: '%s'. No cases will be fetched.", fromDate, effectiveToDate));
			return;
		}

		Arrays.stream(Instance.values()).forEach(instance -> handleCasesByInstance(instance, fromDate, effectiveToDate, municipalityId, importHealthConsumer));

		fetchAndSaveStatsOnlyCaseIds(fromDate, effectiveToDate, municipalityId, importHealthConsumer);
	}

	public boolean updateOpenECaseStatus(final String flowInstanceId, final CaseMetaDataEntity caseMetaDataEntity) {

		try {
			if (!isEmpty(caseMetaDataEntity.getOpenEUpdateStatus())) {
				final var setStatus = new CaseStatusChangeRequest().name(caseMetaDataEntity.getOpenEUpdateStatus());

				if (EXTERNAL.equals(caseMetaDataEntity.getInstance())) {
					oepIntegratorClient.setStatus(caseMetaDataEntity.getMunicipalityId(), InstanceType.EXTERNAL, flowInstanceId, setStatus);
				} else {
					oepIntegratorClient.setStatus(caseMetaDataEntity.getMunicipalityId(), InstanceType.INTERNAL, flowInstanceId, setStatus);
				}
			}
			return true;
		} catch (final Exception e) {
			LOGGER.error("Error while setting status for flowInstanceId: '{}'", flowInstanceId, e);
			return false;
		}
	}

	public boolean confirmDelivery(final String flowInstanceId, final CaseMetaDataEntity caseMetaDataEntity, final String errandId) {
		try {
			final var confirmDelivery = new ConfirmDeliveryRequest()
				.caseId(errandId)
				.delivered(true)
				.system(SYSTEM_SUPPORT_MANAGEMENT);

			if (EXTERNAL.equals(caseMetaDataEntity.getInstance())) {
				oepIntegratorClient.confirmDelivery(caseMetaDataEntity.getMunicipalityId(), InstanceType.EXTERNAL, flowInstanceId, confirmDelivery);
			} else {
				oepIntegratorClient.confirmDelivery(caseMetaDataEntity.getMunicipalityId(), InstanceType.INTERNAL, flowInstanceId, confirmDelivery);
			}
			return true;
		} catch (final Exception e) {
			LOGGER.error("Error while confirming delivery for flowInstanceId: '{}'", flowInstanceId, e);
			return false;
		}
	}

	private void handleCasesByInstance(final Instance instance, final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId, final Consumer<String> importHealthConsumer) {

		final var metaDataEntities = caseMetaDataRepository.findByInstanceAndMunicipalityIdAndStatsOnly(instance, municipalityId, false);

		final var flowInstanceIds = metaDataEntities.stream()
			.map(metaData -> getFlowInstanceIds(metaData, fromDate.toString(), toDate.toString(), instance, importHealthConsumer))
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.distinct()
			.toList();

		flowInstanceIds.forEach(flowInstanceId -> {
			if (caseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(flowInstanceId, instance, municipalityId)) {
				LOGGER.info("Case with id: '{}' already exists in database. Nothing will be saved.", flowInstanceId);
				return;
			}
			final var openECase = getOpenECase(instance, flowInstanceId, importHealthConsumer, municipalityId);

			final var familyId = ofNullable(openECase).map(oepCase -> openECase.getFamilyId()).orElse(null);
			final var caseMetaData = ofNullable(familyId).flatMap(caseMetaDataRepository::findById);

			if (nonNull(openECase) && caseMetaData.isPresent()) {
				caseRepository.save(toCaseEntity(flowInstanceId, caseMetaData.get(), openECase.getPayload()));
			} else {
				LOGGER.info("Case with id: '{}' not found in OpenE. Nothing will be saved.", flowInstanceId);
			}
		});
	}

	private void fetchAndSaveStatsOnlyCaseIds(final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId, final Consumer<String> importHealthConsumer) {

		final var statsOnlyMetaDataEntities = caseMetaDataRepository.findByMunicipalityIdAndStatsOnly(municipalityId, true);

		statsOnlyMetaDataEntities.forEach(metaData -> getFlowInstanceIds(metaData, fromDate.toString(), toDate.toString(), metaData.getInstance(), importHealthConsumer).stream()
			.filter(Objects::nonNull)
			.distinct()
			.forEach(flowInstanceId -> processStatsOnlyCase(flowInstanceId, metaData)));
	}

	private List<String> getFlowInstanceIds(final CaseMetaDataEntity caseMetaDataEntity, final String fromDate, final String toDate, final Instance instance, final Consumer<String> importHealthConsumer) {
		try {
			if (instance == EXTERNAL) {
				return oepIntegratorClient.getCases(caseMetaDataEntity.getMunicipalityId(), InstanceType.EXTERNAL, Integer.parseInt(caseMetaDataEntity.getFamilyId()), formatLocalDate(fromDate), formatLocalDate(toDate),
					caseMetaDataEntity.getOpenEImportStatus())
					.stream()
					.map(CaseEnvelope::getFlowInstanceId)
					.toList();
			} else {
				return oepIntegratorClient.getCases(caseMetaDataEntity.getMunicipalityId(), InstanceType.INTERNAL, Integer.parseInt(caseMetaDataEntity.getFamilyId()), formatLocalDate(fromDate), formatLocalDate(toDate),
					caseMetaDataEntity.getOpenEImportStatus())
					.stream()
					.map(CaseEnvelope::getFlowInstanceId)
					.toList();
			}
		} catch (final Exception e) {
			LOGGER.error("Error while fetching errandIds for familyId: '{}'", caseMetaDataEntity.getFamilyId(), e);
			importHealthConsumer.accept("Error while fetching errands by familyId " + caseMetaDataEntity.getFamilyId());
			return emptyList();
		}
	}

	private void processStatsOnlyCase(final String flowInstanceId, final CaseMetaDataEntity caseMetaDataEntity) {
		if (caseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(flowInstanceId, caseMetaDataEntity.getInstance(), caseMetaDataEntity.getMunicipalityId())) {
			LOGGER.info("Case with id: '{}' already exists in database. Nothing will be saved.", flowInstanceId);
			return;
		}

		caseRepository.save(toCaseEntity(flowInstanceId, caseMetaDataEntity, null));
	}

	Response getFile(final String flowInstanceId, final String fileId, final String queryId, final Instance instance, final String municipalityId) {

		if (instance == EXTERNAL) {
			return oepIntegratorClient.getAttachment(municipalityId, InstanceType.EXTERNAL, flowInstanceId, queryId, fileId);

		} else {
			return oepIntegratorClient.getAttachment(municipalityId, InstanceType.INTERNAL, flowInstanceId, queryId, fileId);
		}
	}

	private ModelCase getOpenECase(final Instance instance, final String flowInstanceId, final Consumer<String> importHealthConsumer, final String municipalityId) {
		try {
			if (instance == EXTERNAL) {
				return oepIntegratorClient.getCase(municipalityId, InstanceType.EXTERNAL, flowInstanceId);
			} else {
				return oepIntegratorClient.getCase(municipalityId, InstanceType.INTERNAL, flowInstanceId);
			}
		} catch (final Exception e) {
			LOGGER.error("Error while fetching errand for flowInstanceId: '{}'", flowInstanceId, e);
			importHealthConsumer.accept("Error while fetching errand by flowInstanceId");
			return null;
		}
	}

	String formatLocalDate(final String date) {
		if (date == null)
			return null;
		try {
			return LocalDateTime.parse(date).toLocalDate().format(ISO_LOCAL_DATE);
		} catch (final Exception e) {
			try {
				return LocalDate.parse(date).format(ISO_LOCAL_DATE);
			} catch (final Exception ignored) {
				return null;
			}
		}
	}
}
