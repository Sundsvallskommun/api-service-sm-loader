package se.sundsvall.smloader.service;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
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

	public void fetchAndSaveNewOpenECases(final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId) {
		RequestId.init();
		final var effectiveToDate = nonNull(toDate) ? toDate : LocalDateTime.now();

		if (fromDate.isAfter(effectiveToDate)) {
			LOGGER.error("From-date: '{}' is after to-date: '{}'. No cases will be fetched.", fromDate, effectiveToDate);
			return;
		}

		Arrays.stream(Instance.values()).forEach(instance -> handleCasesByInstance(instance, fromDate, effectiveToDate, municipalityId));
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

	private void handleCasesByInstance(final Instance instance, final LocalDateTime fromDate, final LocalDateTime toDate, final String municipalityId) {

		final var metaDataEntities = caseMetaDataRepository.findByInstanceAndMunicipalityId(instance, municipalityId);

		final var flowInstanceIds = metaDataEntities.stream()
			.map(metaData -> {
				try {
					if (instance == EXTERNAL) {
						return openEExternalClient.getErrandIds(metaData.getFamilyId(), metaData.getOpenEImportStatus(), fromDate.toString(), toDate.toString());
					} else {
						return openEInternalClient.getErrandIds(metaData.getFamilyId(), metaData.getOpenEImportStatus(), fromDate.toString(), toDate.toString());
					}
				} catch (final Exception e) {
					LOGGER.error("Error while fetching errandIds for familyId: '{}'", metaData.getFamilyId(), e);
					return null;
				}
			})
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
			final var openECase = getOpenECase(instance, flowInstanceId);
			final var familyId = ofNullable(openECase).map(oepCase -> getFamilyId(openECase)).orElse(null);
			final var caseMetaData = ofNullable(familyId).map(caseMetaDataRepository::findById).orElse(empty());

			if (nonNull(openECase) && caseMetaData.isPresent()) {
				caseRepository.save(toCaseEntity(flowInstanceId, caseMetaData.get(), openECase));
			}
		});
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

	private byte[] getOpenECase(final Instance instance, final String flowInstanceId) {
		try {
			return instance == EXTERNAL ? openEExternalClient.getErrand(flowInstanceId) : openEInternalClient.getErrand(flowInstanceId);
		} catch (final Exception e) {
			LOGGER.error("Error while fetching errand for flowInstanceId: '{}'", flowInstanceId, e);
			return null;
		}
	}
}
