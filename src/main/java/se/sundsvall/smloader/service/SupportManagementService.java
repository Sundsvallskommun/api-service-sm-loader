package se.sundsvall.smloader.service;

import generated.se.sundsvall.supportmanagement.Errand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.DeliveryStatus;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.LOCATION;
import static se.sundsvall.smloader.integration.db.model.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.service.mapper.CaseMapper.toCaseMapping;

@Service
public class SupportManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupportManagementService.class);
	private final SupportManagementClient supportManagementClient;
	private final CaseRepository caseRepository;
	private final CaseMappingRepository caseMappingRepository;
	private final Map<String, OpenEMapper> openEMapperMap;
	private final NamespaceProperties namespaceProperties;

	public SupportManagementService(SupportManagementClient supportManagementClient, CaseRepository caseRepository, CaseMappingRepository caseMappingRepository, final List<OpenEMapper> openEMappers,
		final NamespaceProperties namespaceProperties) {
		this.supportManagementClient = supportManagementClient;
		this.caseRepository = caseRepository;
		this.caseMappingRepository = caseMappingRepository;
		this.openEMapperMap = openEMappers.stream().collect(toMap(OpenEMapper::getSupportedFamilyId, Function.identity()));
		this.namespaceProperties = namespaceProperties;
	}

	public void exportCases() {
		final var casesToExport = caseRepository.findAllByDeliveryStatus(DeliveryStatus.PENDING);

		casesToExport.forEach(caseEntity -> {
			final var mapper = openEMapperMap.get(caseEntity.getFamilyId());
			if (mapper == null) {
				caseRepository.save(caseEntity.withDeliveryStatus(FAILED));
				LOGGER.error("No mapper found for familyId: {}", caseEntity.getFamilyId());
				return;
			}

			final var errand = mapper.mapToErrand(Optional.ofNullable(caseEntity.getOpenECase()).orElse("").getBytes());
			final var errandId = sendToSupportManagement(errand, caseEntity.getFamilyId());
			if (errandId == null) {
				caseRepository.save(caseEntity.withDeliveryStatus(FAILED));
				return;
			}
			final var caseMapping = toCaseMapping(errandId, caseEntity);
			caseMappingRepository.save(caseMapping);
			caseRepository.save(caseEntity.withDeliveryStatus(CREATED));
		});
	}

	private String sendToSupportManagement(Errand errand, String familyId) {
		try {
			final var namespace = getNamespace(familyId);
			if (namespace == null) {
				LOGGER.error("No namespace found for familyId: {}", familyId);
				return null;
			}
			final var result = supportManagementClient.createErrand(namespace, MUNICIPALITY_ID, errand);
			final var location = String.valueOf(result.getHeaders().getFirst(LOCATION));
			return location.substring(location.lastIndexOf("/") + 1);
		} catch (Exception e) {
			LOGGER.error("Failed to send errand to SupportManagement", e);
			return null;
		}
	}

	private String getNamespace(String familyId) {
		return namespaceProperties.getNamespace().keySet().stream().filter(key -> namespaceProperties.getNamespace().get(key).contains(familyId)).findFirst().orElse(null);
	}
}
