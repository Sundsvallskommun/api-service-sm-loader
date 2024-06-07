package se.sundsvall.smloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.openeexternal.OpenEExternalClient;
import se.sundsvall.smloader.integration.openeexternalsoap.OpenEExternalSoapClient;
import se.sundsvall.smloader.integration.openeinternal.OpenEInternalClient;
import se.sundsvall.smloader.integration.openeinternalsoap.OpenEInternalSoapClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;
import static se.sundsvall.smloader.integration.db.model.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.db.model.Instance.INTERNAL;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseEntity;

@Service
public class OpenEService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenEService.class);

	@Value("${integration.open-e-external.family-ids:}")
	private String externalFamilyIds;

	@Value("${integration.open-e-internal.family-ids:}")
	private String internalFamilyIds;

	private final OpenEExternalClient openEExternalClient;
	private final OpenEInternalClient openEInternalClient;
	private final OpenEExternalSoapClient openEExternalSoapClient;
	private final OpenEInternalSoapClient openEInternalSoapClient;
	private final CaseRepository caseRepository;

	public OpenEService(OpenEExternalClient openEExternalClient, OpenEInternalClient openEInternalClient, OpenEExternalSoapClient openEExternalSoapClient, OpenEInternalSoapClient openEInternalSoapClient,
			CaseRepository caseRepository) {
		this.openEExternalClient = openEExternalClient;
		this.openEInternalClient = openEInternalClient;
		this.openEExternalSoapClient = openEExternalSoapClient;
		this.openEInternalSoapClient = openEInternalSoapClient;
		this.caseRepository = caseRepository;
	}

	public void fetchAndSaveNewOpenECases(String status, LocalDateTime fromDate, LocalDateTime toDate) {
		handleExternalCases(status, fromDate, toDate);

		handleInternalCases(status, fromDate, toDate);
	}

	private void handleExternalCases(String status, LocalDateTime fromDate, LocalDateTime toDate) {

		final var externalFlowInstanceIds = Arrays.stream(this.externalFamilyIds.split(","))
			.map(familyId -> openEExternalClient.getErrandIds(familyId, status, fromDate.toString(), toDate.toString()))
			.map(this::getErrandIds)
			.flatMap(List::stream)
			.distinct()
			.toList();

		externalFlowInstanceIds.forEach(flowInstanceId -> {
			if (caseRepository.existsByOpenECaseIdAndInstance(flowInstanceId, EXTERNAL)) {
				LOGGER.info("Case with id: '{}' already exists in database. Nothing will be saved.", flowInstanceId);
				return;
			}
			final var openECase = openEExternalClient.getErrand(flowInstanceId);
			if (nonNull(openECase)) {
				caseRepository.save(toCaseEntity(flowInstanceId, EXTERNAL, openECase));
			}
		});
	}

	private void handleInternalCases(String status, LocalDateTime fromDate, LocalDateTime toDate) {
		final var internalFlowInstanceIds = Arrays.stream(this.internalFamilyIds.split(","))
			.map(familyId -> openEInternalClient.getErrandIds(familyId, status, fromDate.toString(), toDate.toString()))
			.map(this::getErrandIds)
			.flatMap(List::stream)
			.distinct()
			.toList();

		internalFlowInstanceIds.forEach(flowInstanceId -> {
			if (caseRepository.existsByOpenECaseIdAndInstance(flowInstanceId, INTERNAL)) {
				LOGGER.info("Case with id: '{}' already exists in database. Nothing will be saved.", flowInstanceId);
				return;
			}
			final var openECase = openEInternalClient.getErrand(flowInstanceId);
			if (nonNull(openECase)) {
				caseRepository.save(toCaseEntity(flowInstanceId, INTERNAL, openECase));
			}
		});
	}

	private List<String> getErrandIds(final byte[] xml) {
		var result = evaluateXPath(xml, "/FlowInstances/FlowInstance/flowInstanceID");

		return result.eachText().stream()
			.map(String::trim)
			.toList();
	}
}
