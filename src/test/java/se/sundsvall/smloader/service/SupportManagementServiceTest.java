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
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.DeliveryStatus.PENDING;

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
	private NamespaceProperties namespaceProperties;



	private SupportManagementService supportManagementService;

	@BeforeEach
	void setUp() {
		when(mockMapper.getSupportedFamilyId()).thenReturn("161");

		supportManagementService = new SupportManagementService(mockSupportManagementClient, mockCaseRepository, mockCaseMappingRepository, List.of(mockMapper), namespaceProperties);
	}
	@Test
	void exportCases() {
		// Arrange
		final var flowInstanceXml ="flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "CONTACTCENTER";
		final var municipalityId = "2281";
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, flowInstanceXml));
		when(mockCaseRepository.findAllByDeliveryStatus(PENDING)).thenReturn(casesToExport);
		when(namespaceProperties.getNamespace()).thenReturn(Map.of(namespace, List.of(familyId)));

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
		when(mockSupportManagementClient.createErrand(namespace, municipalityId, errand)).thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());

		// Act
		supportManagementService.exportCases();

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatus(PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).createErrand(namespace, municipalityId, errand);
		verify(mockCaseMappingRepository).save(any());
		verify(mockCaseRepository).save(any());
	}

	@Test
	void exportCasesWhenNoMapperFound() {
		// Arrange
		final var flowInstanceXml ="flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "familyIdWithoutMapper";
		final var flowInstanceId = "123456";
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, flowInstanceXml);
		final var casesToExport = List.of(caseEntity);
		when(mockCaseRepository.findAllByDeliveryStatus(PENDING)).thenReturn(casesToExport);

		// Act
		supportManagementService.exportCases();

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatus(PENDING);
		verify(mockCaseRepository).save(caseEntity.withDeliveryStatus(FAILED));
		verify(mockMapper).getSupportedFamilyId();
		verifyNoMoreInteractions(mockMapper);
		verifyNoInteractions(mockSupportManagementClient, mockCaseMappingRepository);
	}

	@Test
	void exportCasesWhenExceptionInSending() {
		// Arrange
		final var flowInstanceXml ="flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "CONTACTCENTER";
		final var municipalityId = "2281";
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, flowInstanceXml));
		when(mockCaseRepository.findAllByDeliveryStatus(PENDING)).thenReturn(casesToExport);
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
		when(namespaceProperties.getNamespace()).thenReturn(Map.of(namespace, List.of(familyId)));
		when(mockSupportManagementClient.createErrand(namespace, municipalityId, errand)).thenThrow(new RuntimeException("Failed to send errand"));

		// Act
		supportManagementService.exportCases();

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatus(PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).createErrand(namespace, municipalityId, errand);
		verify(mockCaseRepository).save(casesToExport.getFirst().withDeliveryStatus(FAILED));
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper);
	}

	private CaseEntity createCaseEntity(String flowInstanceId, String familyId, byte[] flowInstanceXml) {
		return CaseEntity.create()
			.withId(flowInstanceId)
			.withFamilyId(familyId)
			.withOpenECase(new String(flowInstanceXml))
			.withDeliveryStatus(PENDING);
	}
}
