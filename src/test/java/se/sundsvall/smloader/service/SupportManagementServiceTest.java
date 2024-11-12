package se.sundsvall.smloader.service;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.SlackRequest;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.messaging.MessagingClient;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.MessagingMapper;
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
	private AttachmentService mockAttachmentService;

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

	@Mock
	private MessagingClient mockMessagingClient;

	@Mock
	private MessagingMapper mockMessagingMapper;

	@Mock
	private Environment mockEnvironment;

	private SupportManagementService supportManagementService;

	@BeforeEach
	void setUp() {
		when(mockMapper.getSupportedFamilyId()).thenReturn("161");
		supportManagementService = new SupportManagementService(mockSupportManagementClient, mockCaseRepository, mockCaseMappingRepository, List.of(mockMapper), mockOpenEService, mockMessagingClient, mockMessagingMapper, mockEnvironment,
			mockAttachmentService);
	}

	@Test
	void exportCases() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml";
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var errandNumber = "errandNumber";
		final var errandId = "errandId";
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
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand)).thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());
		when(mockSupportManagementClient.getErrand(municipalityId, namespace, errandId)).thenReturn(errand.errandNumber(errandNumber).id(errandId));

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockAttachmentService).handleAttachments(flowInstanceXml.getBytes(), casesToExport.getFirst(), errandId);
		verify(mockMapper).mapToErrand(flowInstanceXml.getBytes());
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockSupportManagementClient).getErrand(municipalityId, namespace, "errandId");
		verify(mockCaseMappingRepository).save(any());
		verify(mockCaseRepository).save(any());
		verify(mockOpenEService).updateOpenECaseStatus(flowInstanceId, CaseMetaDataEntity.create().withFamilyId(familyId).withInstance(EXTERNAL).withNamespace(namespace).withMunicipalityId(municipalityId));
		verify(mockOpenEService).confirmDelivery(flowInstanceId, EXTERNAL, errandNumber);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper);
	}

	@Test
	void exportCasesWhenNoMapperFound() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
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
		verifyNoMoreInteractions(mockMapper, mockCaseRepository, mockCaseMappingRepository, mockSupportManagementClient, mockOpenEService, mockMessagingClient, mockMessagingMapper, mockAttachmentService);
	}

	@Test
	void exportCasesWhenExceptionInSending() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml)));
		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});
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
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand)).thenThrow(new RuntimeException("Failed to send errand"));
		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockCaseRepository).save(casesToExport.getFirst().withDeliveryStatus(FAILED));
		verify(mockMessagingMapper).toRequest("SmLoader failed to export cases: [123456]");
		verify(mockMessagingMapper).toEmailRequest("SmLoader - Test", "Failed to export cases: [123456]");
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper, mockAttachmentService);
	}

	private CaseEntity createCaseEntity(final String flowInstanceId, final String familyId, final byte[] flowInstanceXml) {
		return CaseEntity.create()
			.withId("id")
			.withExternalCaseId(flowInstanceId)
			.withCaseMetaData(CaseMetaDataEntity.create().withFamilyId(familyId).withInstance(EXTERNAL).withNamespace("namespace").withMunicipalityId("municipalityId"))
			.withOpenECase(new String(flowInstanceXml))
			.withDeliveryStatus(PENDING);
	}
}
