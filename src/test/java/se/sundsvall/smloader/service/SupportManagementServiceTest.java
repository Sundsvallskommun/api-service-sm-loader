package se.sundsvall.smloader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.SlackRequest;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMappingId;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.messaging.MessagingClient;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;
import se.sundsvall.smloader.service.mapper.MessagingMapper;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

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
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml.getBytes()));
		final var casesToExport = List.of(caseEntity);
		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);

		final var errand = new Errand()
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.externalTags(Set.of(new ExternalTag().key("familyId").value(familyId), new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE_INTERNAL")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))));

		when(mockMapper.mapToErrand(flowInstanceXml.getBytes())).thenReturn(errand);
		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(Page.empty());
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand)).thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());
		when(mockSupportManagementClient.getErrand(municipalityId, namespace, errandId)).thenReturn(errand.errandNumber(errandNumber).id(errandId));
		when(mockCaseMappingRepository.existsById(any())).thenReturn(false);
		when(mockOpenEService.updateOpenECaseStatus(any(), any())).thenReturn(true);
		when(mockOpenEService.confirmDelivery(any(), any(), any())).thenReturn(true);

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockAttachmentService).handleAttachments(flowInstanceXml.getBytes(), casesToExport.getFirst(), errandId);
		verify(mockMapper).mapToErrand(flowInstanceXml.getBytes());
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockSupportManagementClient).getErrand(municipalityId, namespace, "errandId");
		verify(mockCaseMappingRepository).existsById(CaseMappingId.create().withExternalCaseId(caseEntity.getExternalCaseId()).withErrandId("errandId"));
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
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");

		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});
		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockCaseRepository).save(caseEntity.withDeliveryStatus(FAILED));
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
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
			.externalTags(Set.of(new ExternalTag().key("familyId").value(familyId), new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE_INTERNAL")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))));

		when(mockMapper.mapToErrand(flowInstanceXml)).thenReturn(errand);
		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(Page.empty());
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand)).thenThrow(new RuntimeException("Failed to send errand"));
		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockCaseRepository).save(casesToExport.getFirst().withDeliveryStatus(FAILED));
		verify(mockMessagingMapper).toRequest(matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"), matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
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

	@Test
	void exportCasesWhenFaultyAttachments() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var errandId = "errandId";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml)));
		when(mockCaseRepository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId)).thenReturn(casesToExport);

		final var errand = new Errand()
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.externalTags(Set.of(new ExternalTag().key("familyId").value(familyId), new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE_INTERNAL")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))));

		when(mockMapper.mapToErrand(flowInstanceXml)).thenReturn(errand);
		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(Page.empty());
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand)).thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());
		when(mockAttachmentService.handleAttachments(flowInstanceXml, casesToExport.getFirst(), errandId)).thenReturn(List.of("attachmentId"));
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});

		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);
		when(mockCaseMappingRepository.existsById(any())).thenReturn(false);

		// Act
		supportManagementService.exportCases(municipalityId);

		// Assert and verify
		verify(mockCaseRepository).findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, municipalityId);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockAttachmentService).handleAttachments(flowInstanceXml, casesToExport.getFirst(), errandId);
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockCaseMappingRepository).save(any());
		verify(mockCaseRepository).save(casesToExport.getFirst());
		verify(mockMessagingMapper).toRequest(
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nFor case errandId the attachments \\[attachmentId\\] were not exported\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"),
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nFor case errandId the attachments \\[attachmentId\\] were not exported\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper, mockAttachmentService);
	}
}
