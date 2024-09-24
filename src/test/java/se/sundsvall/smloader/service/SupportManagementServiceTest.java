package se.sundsvall.smloader.service;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.net.URI;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;

@ExtendWith(MockitoExtension.class)
class SupportManagementServiceTest {

	@Mock
	private CaseRepository mockCaseRepository;

	@Mock
	private CaseMappingRepository mockCaseMappingRepository;

	@Mock
	private SupportManagementClient mockSupportManagementClient;

	@Mock
	private OpenEMapper mockMapper;

	@Mock
	private OpenEService mockOpenEService;

	private SupportManagementService supportManagementService;

	@BeforeEach
	void setUp() {
		when(mockMapper.getSupportedFamilyId()).thenReturn("161");

		supportManagementService = new SupportManagementService(mockSupportManagementClient, mockCaseRepository, mockCaseMappingRepository, List.of(mockMapper), mockOpenEService);
	}
	@Test
	void exportCases() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml";
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var errandNumber = "errandNumber";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml.getBytes())));
		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);


		final var errand = new Errand()
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))));

		when(mockMapper.mapToErrand(flowInstanceXml.getBytes())).thenReturn(errand);
		when(mockSupportManagementClient.createErrand(namespace, municipalityId, errand)).thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());
		when(mockSupportManagementClient.getErrand(namespace, municipalityId, "errandId")).thenReturn(errand.errandNumber(errandNumber));

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml.getBytes());
		verify(mockSupportManagementClient).createErrand(namespace, municipalityId, errand);
		verify(mockSupportManagementClient).getErrand(namespace, municipalityId, "errandId");
		verify(mockCaseMappingRepository).save(any());
		verify(mockCaseRepository).save(any());
		verify(mockOpenEService).updateOpenECaseStatus(flowInstanceId, CaseMetaDataEntity.create().withFamilyId(familyId).withInstance(EXTERNAL).withNamespace(namespace).withMunicipalityId(municipalityId));
		verify(mockOpenEService).confirmDelivery(flowInstanceId, EXTERNAL, errandNumber);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService);
	}

	@Test
	void exportCasesWhenNoMapperFound() {
		// Arrange
		final var flowInstanceXml ="flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "familyIdWithoutMapper";
		final var flowInstanceId = "123456";
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, flowInstanceXml);
		final var casesToExport = List.of(caseEntity);
		final var municipalityId = "municipalityId";

		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockCaseRepository).save(caseEntity.withDeliveryStatus(FAILED));
		verify(mockMapper).getSupportedFamilyId();
		verifyNoMoreInteractions(mockMapper, mockCaseRepository, mockCaseMappingRepository, mockSupportManagementClient, mockOpenEService);
	}

	@Test
	void exportCasesWhenExceptionInSending() {
		// Arrange
		final var flowInstanceXml ="flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId,  Base64.getEncoder().encode(flowInstanceXml)));
		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);
		final var errand = new Errand()
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))));

		when(mockMapper.mapToErrand(flowInstanceXml)).thenReturn(errand);
		when(mockSupportManagementClient.createErrand(namespace, municipalityId, errand)).thenThrow(new RuntimeException("Failed to send errand"));

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).createErrand(namespace, municipalityId, errand);
		verify(mockCaseRepository).save(casesToExport.getFirst().withDeliveryStatus(FAILED));
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService);
	}

	private CaseEntity createCaseEntity(String flowInstanceId, String familyId, byte[] flowInstanceXml) {
		return CaseEntity.create()
			.withId("id")
			.withExternalCaseId(flowInstanceId)
			.withCaseMetaData(CaseMetaDataEntity.create().withFamilyId(familyId).withInstance(EXTERNAL).withNamespace("namespace").withMunicipalityId("municipalityId"))
			.withOpenECase(new String(flowInstanceXml))
			.withDeliveryStatus(PENDING);
	}
}
