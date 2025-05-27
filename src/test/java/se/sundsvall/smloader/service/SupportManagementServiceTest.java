package se.sundsvall.smloader.service;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
import se.sundsvall.smloader.integration.openemapper.statsonly.StatsOnlyMapper;
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
	private StatsOnlyMapper mockStatsOnlyMapper;

	@Mock
	private OpenEService mockOpenEService;

	@Mock
	private MessagingClient mockMessagingClient;

	@Mock
	private MessagingMapper mockMessagingMapper;

	@Mock
	private Environment mockEnvironment;

	@Mock
	private Consumer<String> consumerMock;

	private SupportManagementService supportManagementService;

	@BeforeEach
	void setUp() {
		when(mockMapper.getSupportedFamilyId()).thenReturn("161");
		supportManagementService = new SupportManagementService(mockSupportManagementClient, mockCaseRepository, mockCaseMappingRepository, List.of(mockMapper), mockOpenEService, mockMessagingClient, mockMessagingMapper, mockEnvironment,
			mockAttachmentService, mockStatsOnlyMapper);
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
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml.getBytes()), false);
		final var casesToExport = List.of(caseEntity);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);

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
					.value("a.b@c")))))
			.activeNotifications(emptyList());

		when(mockMapper.mapToErrand(flowInstanceXml.getBytes())).thenReturn(errand);
		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(Page.empty());
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand.activeNotifications(null))).thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());
		when(mockSupportManagementClient.getErrand(municipalityId, namespace, errandId)).thenReturn(errand.errandNumber(errandNumber).id(errandId));
		when(mockCaseMappingRepository.existsById(any())).thenReturn(false);
		when(mockOpenEService.updateOpenECaseStatus(any(), any())).thenReturn(true);
		when(mockOpenEService.confirmDelivery(any(), any(), any())).thenReturn(true);

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockAttachmentService).handleAttachments(flowInstanceXml.getBytes(), casesToExport.getFirst(), errandId);
		verify(mockMapper).mapToErrand(flowInstanceXml.getBytes());
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand.activeNotifications(null));
		verify(mockSupportManagementClient).getErrand(municipalityId, namespace, "errandId");
		verify(mockCaseMappingRepository).existsById(CaseMappingId.create().withExternalCaseId(caseEntity.getExternalCaseId()).withErrandId("errandId"));
		verify(mockCaseMappingRepository).save(any());
		verify(mockCaseRepository).save(any());
		verify(mockOpenEService).updateOpenECaseStatus(flowInstanceId, CaseMetaDataEntity.create().withFamilyId(familyId).withInstance(EXTERNAL).withNamespace(namespace).withMunicipalityId(municipalityId));
		verify(mockOpenEService).confirmDelivery(flowInstanceId, EXTERNAL, errandNumber);
		verifyNoInteractions(consumerMock);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper);
	}

	@Test
	void exportCasesStatsOnly() {
		// Arrange
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var errandNumber = "errandNumber";
		final var errandId = "errandId";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, null, true);
		final var casesToExport = List.of(caseEntity);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);

		final var errand = new Errand()
			.classification(new Classification()
				.category("category")
				.type("type"))
			.externalTags(Set.of(new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE")
			.activeNotifications(emptyList());

		when(mockStatsOnlyMapper.mapToErrand(caseEntity, familyId, EXTERNAL)).thenReturn(Optional.of(errand));
		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(Page.empty());
		when(mockSupportManagementClient.createErrand(municipalityId, namespace, errand.activeNotifications(null)))
			.thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/errands/errandId")).build());
		when(mockSupportManagementClient.getErrand(municipalityId, namespace, errandId)).thenReturn(errand.errandNumber(errandNumber).id(errandId));
		when(mockCaseMappingRepository.existsById(any())).thenReturn(false);
		when(mockOpenEService.updateOpenECaseStatus(any(), any())).thenReturn(true);
		when(mockOpenEService.confirmDelivery(any(), any(), any())).thenReturn(true);

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockStatsOnlyMapper).mapToErrand(caseEntity, familyId, EXTERNAL);
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand.activeNotifications(null));
		verify(mockSupportManagementClient).getErrand(municipalityId, namespace, "errandId");
		verify(mockCaseMappingRepository).existsById(CaseMappingId.create().withExternalCaseId(caseEntity.getExternalCaseId()).withErrandId("errandId"));
		verify(mockCaseMappingRepository).save(any());
		verify(mockCaseRepository).save(any());
		verify(mockOpenEService).updateOpenECaseStatus(flowInstanceId, CaseMetaDataEntity.create()
			.withFamilyId(familyId)
			.withInstance(EXTERNAL)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withStatsOnly(true));
		verify(mockOpenEService).confirmDelivery(flowInstanceId, EXTERNAL, errandNumber);
		verifyNoInteractions(consumerMock);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper);
	}

	@Test
	void exportCasesStatsOnlyWhenErrorInMapping() {
		// Arrange
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var municipalityId = "municipalityId";
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, null, true);
		final var casesToExport = List.of(caseEntity);
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");

		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);
		when(mockStatsOnlyMapper.mapToErrand(caseEntity, familyId, EXTERNAL)).thenReturn(Optional.empty());
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});
		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockStatsOnlyMapper).mapToErrand(caseEntity, familyId, EXTERNAL);
		verify(consumerMock).accept("Failed to export 1 errands!");
		verify(mockCaseRepository).save(casesToExport.getFirst());
		verify(mockMessagingMapper).toRequest(
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"),
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));

		verifyNoInteractions(mockCaseMappingRepository, mockSupportManagementClient, mockOpenEService);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockMapper, mockMessagingMapper, consumerMock);
	}

	@Test
	void exportCasesWhenNoMapperFound() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "familyIdWithoutMapper";
		final var flowInstanceId = "123456";
		final var caseEntity = createCaseEntity(flowInstanceId, familyId, flowInstanceXml, false);
		final var casesToExport = List.of(caseEntity);
		final var municipalityId = "municipalityId";
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");

		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});
		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockCaseRepository).save(caseEntity.withDeliveryStatus(FAILED));
		verify(mockMapper).getSupportedFamilyId();
		verify(consumerMock).accept("Failed to export 1 errands!");
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
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml), false));
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);
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
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockCaseRepository).save(casesToExport.getFirst().withDeliveryStatus(FAILED));
		verify(consumerMock).accept("Failed to export 1 errands!");
		verify(mockMessagingMapper).toRequest(matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"), matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper, mockAttachmentService);
	}

	@Test
	void exportCasesWithoutReReportingFailedCases() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes(); // "flow-instance-lamna-synpunkt.xml
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml), false));
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(emptyList());

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

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockCaseRepository).save(casesToExport.getFirst().withDeliveryStatus(FAILED));
		verify(consumerMock).accept("Failed to export 1 errands!");
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper, mockAttachmentService);
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
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml), false));
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);

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
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockAttachmentService).handleAttachments(flowInstanceXml, casesToExport.getFirst(), errandId);
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(externalTags.key:'caseId' and externalTags.value:'123456') and exists(externalTags.key:'familyId' and externalTags.value:'161') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).createErrand(municipalityId, namespace, errand);
		verify(mockCaseMappingRepository).save(any());
		verify(consumerMock).accept("Failed to export 1 errands!");
		verify(mockCaseRepository).save(casesToExport.getFirst());
		verify(mockMessagingMapper).toRequest(
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nFor case errandId the attachments \\[attachmentId\\] were not exported\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"),
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nFor case errandId the attachments \\[attachmentId\\] were not exported\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
		verifyNoMoreInteractions(mockCaseMappingRepository, mockCaseRepository, mockSupportManagementClient, mockMapper, mockOpenEService, mockMessagingClient, mockMessagingMapper, mockAttachmentService);
	}

	@Test
	void exportCasesWhenMapperThrowsException() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes();
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var municipalityId = "municipalityId";
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");
		final var casesToExport = List.of(createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml), false));
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);

		when(mockMapper.mapToErrand(any())).thenThrow(new RuntimeException("Error"));
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});

		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(consumerMock).accept("Failed to export 1 errands!");
		verify(mockCaseRepository).save(casesToExport.getFirst());
		verify(mockMessagingMapper).toRequest(
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"),
			matches("SmLoader failed to export cases: \\[123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
		verify(mockEnvironment).getActiveProfiles();
		verifyNoInteractions(mockSupportManagementClient, mockAttachmentService, mockOpenEService, mockCaseMappingRepository);
		verifyNoMoreInteractions(mockCaseRepository, mockMapper, mockMessagingClient, mockMessagingMapper, mockAttachmentService, mockEnvironment);
	}

	@Test
	void exportCasesWhenBothStatsOnlyAndOtherMapperNotWorking() {
		// Arrange
		final var flowInstanceXml = "flowInstanceXml".getBytes();
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var familyIdStatsOnly = "456";
		final var flowInstanceIdStatsOnly = "654321";
		final var municipalityId = "municipalityId";
		final var slackRequest = new SlackRequest().message("Failed to send errand");
		final var emailRequest = new EmailRequest().message("Failed to send errand");
		final var notStatsOnlyCaseEntity = createCaseEntity(flowInstanceId, familyId, Base64.getEncoder().encode(flowInstanceXml), false);
		final var statsOnlyCaseEntity = createCaseEntity(flowInstanceIdStatsOnly, familyIdStatsOnly, null, true);
		final var casesToExport = List.of(notStatsOnlyCaseEntity, statsOnlyCaseEntity);

		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED)).thenReturn(casesToExport);
		when(mockCaseRepository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING)).thenReturn(casesToExport);

		when(mockMapper.mapToErrand(any())).thenThrow(new RuntimeException("Error"));
		when(mockStatsOnlyMapper.mapToErrand(any(), any(), any())).thenReturn(Optional.empty());
		when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {
			"test"
		});

		when(mockMessagingMapper.toRequest(any())).thenReturn(slackRequest);
		when(mockMessagingMapper.toEmailRequest(any(), any())).thenReturn(emailRequest);

		// Act
		supportManagementService.exportCases(municipalityId, consumerMock);

		// Assert and verify
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING, FAILED);
		verify(mockCaseRepository).findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(municipalityId, PENDING);
		verify(mockMapper).getSupportedFamilyId();
		verify(mockMapper).mapToErrand(flowInstanceXml);
		verify(mockStatsOnlyMapper).mapToErrand(statsOnlyCaseEntity, familyIdStatsOnly, EXTERNAL);
		verify(consumerMock).accept("Failed to export 1 errands!");
		verify(consumerMock).accept("Failed to export 2 errands!");
		verify(mockCaseRepository, times(2)).save(any(CaseEntity.class));
		verify(mockMessagingMapper).toRequest(
			matches("SmLoader failed to export cases: \\[654321, 123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingMapper).toEmailRequest(eq("SmLoader - Test"),
			matches("SmLoader failed to export cases: \\[654321, 123456\\]\\.\\nRequestId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
		verify(mockMessagingClient).sendSlack(municipalityId, slackRequest);
		verify(mockMessagingClient).sendEmail(municipalityId, emailRequest);
		verify(mockEnvironment).getActiveProfiles();
		verifyNoInteractions(mockSupportManagementClient, mockAttachmentService, mockOpenEService, mockCaseMappingRepository);
		verifyNoMoreInteractions(mockCaseRepository, mockMapper, mockMessagingClient, mockMessagingMapper, mockAttachmentService, mockEnvironment);
	}

	private CaseEntity createCaseEntity(final String flowInstanceId, final String familyId, final byte[] flowInstanceXml, final boolean statsOnly) {
		var caseEntity = CaseEntity.create()
			.withId("id")
			.withExternalCaseId(flowInstanceId)
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withFamilyId(familyId)
				.withInstance(EXTERNAL)
				.withNamespace("namespace")
				.withMunicipalityId("municipalityId")
				.withStatsOnly(statsOnly))
			.withDeliveryStatus(PENDING);

		if (!statsOnly) {
			caseEntity.setOpenECase(new String(flowInstanceXml));
		}
		return caseEntity;
	}
}
