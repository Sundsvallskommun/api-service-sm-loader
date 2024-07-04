package se.sundsvall.smloader.service;

import generated.se.sundsvall.supportmanagement.Errand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.LOCATION;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseMapping;

@Service
public class SupportManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupportManagementService.class);
	private final SupportManagementClient supportManagementClient;
	private final CaseRepository caseRepository;
	private final CaseMappingRepository caseMappingRepository;
	private final Map<String, OpenEMapper> openEMapperMap;
	private final OpenEService openEService;

	public SupportManagementService(final SupportManagementClient supportManagementClient, final CaseRepository caseRepository, final CaseMappingRepository caseMappingRepository, final List<OpenEMapper> openEMappers,
		final OpenEService openEService) {
		this.supportManagementClient = supportManagementClient;
		this.caseRepository = caseRepository;
		this.caseMappingRepository = caseMappingRepository;
		this.openEMapperMap = openEMappers.stream().collect(toMap(OpenEMapper::getSupportedFamilyId, Function.identity()));
		this.openEService = openEService;
	}

	public void exportCases() {
		RequestId.init();
		final var casesToExport = caseRepository.findAllByDeliveryStatus(PENDING);

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
				return;
			}
			final var caseMapping = toCaseMapping(errandId, caseEntity);
			caseMappingRepository.save(caseMapping);
			caseRepository.save(caseEntity.withDeliveryStatus(CREATED));

			openEService.updateOpenECaseStatus(caseEntity.getExternalCaseId(), caseEntity.getCaseMetaData());

			final var createdErrand = getErrandFromSupportManagement(errandId, caseEntity.getCaseMetaData().getNamespace(), caseEntity.getCaseMetaData().getMunicipalityId());

			openEService.confirmDelivery(caseEntity.getExternalCaseId(), caseEntity.getCaseMetaData().getInstance(), Optional.ofNullable(createdErrand).map(Errand::getErrandNumber).orElse(null));
		});
	}

	private String sendToSupportManagement(Errand errand, String namespace, String municipalityId) {
		try {
			final var result = supportManagementClient.createErrand(namespace, municipalityId, errand);
			final var location = String.valueOf(result.getHeaders().getFirst(LOCATION));
			return location.substring(location.lastIndexOf("/") + 1);
		} catch (Exception e) {
			LOGGER.error("Failed to send errand to SupportManagement", e);
			return null;
		}
	}

	private Errand getErrandFromSupportManagement(String errandId, String namespace, String municipalityId) {
		try {
			return supportManagementClient.getErrand(namespace, municipalityId, errandId);
		} catch (Exception e) {
			LOGGER.error("Failed to get errand from SupportManagement", e);
			return null;
		}
	}
}
