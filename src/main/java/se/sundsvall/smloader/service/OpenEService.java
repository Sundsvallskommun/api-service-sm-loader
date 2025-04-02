package se.sundsvall.smloader.service;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.SYSTEM_SUPPORT_MANAGEMENT;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseEntity;

import feign.Response;
import generated.se.sundsvall.callback.ConfirmDelivery;
import generated.se.sundsvall.callback.ExternalID;
import generated.se.sundsvall.callback.SetStatus;
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
import se.sundsvall.smloader.integration.openeexternal.OpenEExternalClient;
import se.sundsvall.smloader.integration.openeexternalsoap.OpenEExternalSoapClient;
import se.sundsvall.smloader.integration.openeinternal.OpenEInternalClient;
import se.sundsvall.smloader.integration.openeinternalsoap.OpenEInternalSoapClient;

@Service
public class OpenEService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenEService.class);

	private final OpenEExternalClient openEExternalClient;
	private final OpenEInternalClient openEInternalClient;
	private final OpenEExternalSoapClient openEExternalSoapClient;
	private final OpenEInternalSoapClient openEInternalSoapClient;
	private final CaseRepository caseRepository;
	private final CaseMetaDataRepository caseMetaDataRepository;

	public OpenEService(final OpenEExternalClient openEExternalClient, final OpenEInternalClient openEInternalClient, final OpenEExternalSoapClient openEExternalSoapClient, final OpenEInternalSoapClient openEInternalSoapClient,
		final CaseRepository caseRepository, final CaseMetaDataRepository caseMetaDataRepository) {
		this.openEExternalClient = openEExternalClient;
		this.openEInternalClient = openEInternalClient;
		this.openEExternalSoapClient = openEExternalSoapClient;
		this.openEInternalSoapClient = openEInternalSoapClient;
		this.caseRepository = caseRepository;
		this.caseMetaDataRepository = caseMetaDataRepository;
	}

	public void fetchAndSaveNewOpenECases(final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId, Consumer<String> importHealthConsumer) {
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
				final var setStatus = new SetStatus().withFlowInstanceID(Integer.parseInt(flowInstanceId)).withStatusAlias(caseMetaDataEntity.getOpenEUpdateStatus());
				if (EXTERNAL.equals(caseMetaDataEntity.getInstance())) {
					openEExternalSoapClient.setStatus(setStatus);
				} else {
					openEInternalSoapClient.setStatus(setStatus);
				}
			}
			return true;
		} catch (final Exception e) {
			LOGGER.error("Error while setting status for flowInstanceId: '{}'", flowInstanceId, e);
			return false;
		}
	}

	public boolean confirmDelivery(final String flowInstanceId, final Instance instance, final String errandId) {
		try {
			final var confirmDelivery = new ConfirmDelivery().withFlowInstanceID(Integer.parseInt(flowInstanceId))
				.withDelivered(true)
				.withExternalID(new ExternalID()
					.withSystem(SYSTEM_SUPPORT_MANAGEMENT)
					.withID(errandId));
			if (EXTERNAL.equals(instance)) {
				openEExternalSoapClient.confirmDelivery(confirmDelivery);
			} else {
				openEInternalSoapClient.confirmDelivery(confirmDelivery);
			}
			return true;
		} catch (final Exception e) {
			LOGGER.error("Error while confirming delivery for flowInstanceId: '{}'", flowInstanceId, e);
			return false;
		}
	}

	private void handleCasesByInstance(final Instance instance, final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId, Consumer<String> importHealthConsumer) {

		final var metaDataEntities = caseMetaDataRepository.findByInstanceAndMunicipalityIdAndStatsOnly(instance, municipalityId, false);

		final var flowInstanceIds = metaDataEntities.stream()
			.map(metaData -> getFlowInstanceIds(metaData, fromDate.toString(), toDate.toString(), instance, importHealthConsumer))
			.filter(Objects::nonNull)
			.map(this::getErrandIds)
			.flatMap(List::stream)
			.distinct()
			.toList();

		flowInstanceIds.forEach(flowInstanceId -> {
			if (caseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(flowInstanceId, instance, municipalityId)) {
				LOGGER.info("Case with id: '{}' already exists in database. Nothing will be saved.", flowInstanceId);
				return;
			}
			final var openECase = getOpenECase(instance, flowInstanceId, importHealthConsumer);
			final var familyId = ofNullable(openECase).map(oepCase -> getFamilyId(openECase)).orElse(null);
			final var caseMetaData = ofNullable(familyId).flatMap(caseMetaDataRepository::findById);

			if (nonNull(openECase) && caseMetaData.isPresent()) {
				caseRepository.save(toCaseEntity(flowInstanceId, caseMetaData.get(), openECase));
			}
		});
	}

	private void fetchAndSaveStatsOnlyCaseIds(final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId, Consumer<String> importHealthConsumer) {

		final var statsOnlyMetaDataEntities = caseMetaDataRepository.findByMunicipalityIdAndStatsOnly(municipalityId, true);

		statsOnlyMetaDataEntities.forEach(metaData -> getErrandIds(getFlowInstanceIds(metaData, fromDate.toString(), toDate.toString(), metaData.getInstance(), importHealthConsumer)).stream()
			.filter(Objects::nonNull)
			.distinct()
			.forEach(flowInstanceId -> processStatsOnlyCase(flowInstanceId, metaData)));
	}

	private byte[] getFlowInstanceIds(CaseMetaDataEntity caseMetaDataEntity, final String fromDate, final String toDate, final Instance instance, Consumer<String> importHealthConsumer) {
		try {
			if (instance == EXTERNAL) {
				return openEExternalClient.getErrandIds(caseMetaDataEntity.getFamilyId(), caseMetaDataEntity.getOpenEImportStatus(), fromDate, toDate);
			} else {
				return openEInternalClient.getErrandIds(caseMetaDataEntity.getFamilyId(), caseMetaDataEntity.getOpenEImportStatus(), fromDate, toDate);
			}
		} catch (final Exception e) {
			LOGGER.error("Error while fetching errandIds for familyId: '{}'", caseMetaDataEntity.getFamilyId(), e);
			importHealthConsumer.accept("Error while fetching errands by familyId");
			return null;
		}
	}

	private void processStatsOnlyCase(final String flowInstanceId, final CaseMetaDataEntity caseMetaDataEntity) {
		if (caseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(flowInstanceId, caseMetaDataEntity.getInstance(), caseMetaDataEntity.getMunicipalityId())) {
			LOGGER.info("Case with id: '{}' already exists in database. Nothing will be saved.", flowInstanceId);
			return;
		}

		caseRepository.save(toCaseEntity(flowInstanceId, caseMetaDataEntity, null));
	}

	private List<String> getErrandIds(final byte[] xml) {
		final var result = evaluateXPath(xml, "/FlowInstances/FlowInstance/flowInstanceID");

		return result.eachText().stream()
			.map(String::trim)
			.toList();
	}

	private String getFamilyId(final byte[] xml) {
		final var result = evaluateXPath(xml, "/FlowInstance/Header/Flow/FamilyID");

		return result.eachText().stream()
			.map(String::trim)
			.findFirst()
			.orElse(null);
	}

	Response getFile(final String flowInstanceId, final String fileId, final String queryId, final Instance instance) {
		return instance == EXTERNAL ? openEExternalClient.getFile(flowInstanceId, queryId, fileId) : openEInternalClient.getFile(flowInstanceId, queryId, fileId);
	}

	private byte[] getOpenECase(final Instance instance, final String flowInstanceId, Consumer<String> importHealthConsumer) {
		try {
			return instance == EXTERNAL ? openEExternalClient.getErrand(flowInstanceId) : openEInternalClient.getErrand(flowInstanceId);
		} catch (final Exception e) {
			LOGGER.error("Error while fetching errand for flowInstanceId: '{}'", flowInstanceId, e);
			importHealthConsumer.accept("Error while fetching errand by flowInstanceId");
			return null;
		}
	}
}
