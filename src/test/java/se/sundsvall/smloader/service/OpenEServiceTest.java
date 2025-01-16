package se.sundsvall.smloader.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.INTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.SYSTEM_SUPPORT_MANAGEMENT;

import feign.Request;
import feign.Response;
import generated.se.sundsvall.callback.ConfirmDelivery;
import generated.se.sundsvall.callback.SetStatus;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;
import se.sundsvall.smloader.integration.openeexternal.OpenEExternalClient;
import se.sundsvall.smloader.integration.openeexternalsoap.OpenEExternalSoapClient;
import se.sundsvall.smloader.integration.openeinternal.OpenEInternalClient;
import se.sundsvall.smloader.integration.openeinternalsoap.OpenEInternalSoapClient;

@ExtendWith(MockitoExtension.class)
class OpenEServiceTest {

	@Mock
	private OpenEExternalClient mockOpenEExternalClient;

	@Mock
	private OpenEInternalClient mockOpenEInternalClient;

	@Mock
	private OpenEExternalSoapClient mockOpenEExternalSoapClient;

	@Mock
	private OpenEInternalSoapClient mockOpenEInternalSoapClient;

	@Mock
	private CaseRepository mockCaseRepository;

	@Mock
	private CaseMetaDataRepository mockCaseMetaDataRepository;

	@Mock
	private Consumer<String> consumerMock;

	@InjectMocks
	private OpenEService openEService;

	@Captor
	private ArgumentCaptor<CaseEntity> caseEntityCaptor;

	@Captor
	private ArgumentCaptor<SetStatus> setStatusCaptor;

	@Captor
	private ArgumentCaptor<ConfirmDelivery> confirmDeliveryCaptor;

	@Test
	void fetchAndSaveNewOpenECases() throws Exception {
		// Arrange
		final var municipalityId = "municipalityId";
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");

		final var flowInstanceXml = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		final var expectedFlowInstance = Base64.getEncoder().encodeToString(flowInstanceXml);

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity456 = CaseMetaDataEntity.create().withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity789 = CaseMetaDataEntity.create().withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity101 = CaseMetaDataEntity.create().withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity112 = CaseMetaDataEntity.create().withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity115 = CaseMetaDataEntity.create().withFamilyId("115").withInstance(INTERNAL).withOpenEImportStatus(status);

		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(EXTERNAL, municipalityId)).thenReturn(List.of(caseMetaDataEntity123, caseMetaDataEntity456, caseMetaDataEntity789));
		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(INTERNAL, municipalityId)).thenReturn(List.of(caseMetaDataEntity101, caseMetaDataEntity112, caseMetaDataEntity115));

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);
		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("115", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);

		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId)).thenReturn(false);

		when(mockCaseMetaDataRepository.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity101))
			.thenReturn(Optional.of(caseMetaDataEntity112))
			.thenReturn(Optional.of(caseMetaDataEntity115))
			.thenReturn(Optional.of(caseMetaDataEntity123))
			.thenReturn(Optional.of(caseMetaDataEntity456))
			.thenReturn(Optional.of(caseMetaDataEntity789));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(6)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(6)).save(caseEntityCaptor.capture());
		verifyNoInteractions(consumerMock);

		assertThat(caseEntityCaptor.getAllValues()).hasSize(6)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", caseMetaDataEntity101, PENDING, expectedFlowInstance),
					tuple("234567", caseMetaDataEntity112, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity115, PENDING, expectedFlowInstance),
					tuple("123456", caseMetaDataEntity123, PENDING, expectedFlowInstance),
					tuple("234567", caseMetaDataEntity456, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity789, PENDING, expectedFlowInstance));
	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExists() throws Exception {
		// Arrange
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");
		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var municipalityId = "municipalityId";

		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(EXTERNAL, municipalityId)).thenReturn(List.of(CaseMetaDataEntity.create().withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status),
			CaseMetaDataEntity.create().withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status),
			CaseMetaDataEntity.create().withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status)));
		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(INTERNAL, municipalityId)).thenReturn(List.of(CaseMetaDataEntity.create().withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status),
			CaseMetaDataEntity.create().withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status)));

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);

		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);

		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId)).thenReturn(true);

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId);
		verify(mockOpenEInternalClient, times(2)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verifyNoInteractions(consumerMock);

		verifyNoMoreInteractions(mockOpenEExternalClient, mockOpenEInternalClient, mockCaseRepository);
	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExceptionOfOneFamilyId() throws Exception {
		// Arrange
		final var municipalityId = "municipalityId";
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");

		final var flowInstanceXml = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		final var expectedFlowInstance = Base64.getEncoder().encodeToString(flowInstanceXml);

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity456 = CaseMetaDataEntity.create().withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity789 = CaseMetaDataEntity.create().withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity101 = CaseMetaDataEntity.create().withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity112 = CaseMetaDataEntity.create().withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity115 = CaseMetaDataEntity.create().withFamilyId("115").withInstance(INTERNAL).withOpenEImportStatus(status);

		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(EXTERNAL, municipalityId)).thenReturn(List.of(caseMetaDataEntity123, caseMetaDataEntity456, caseMetaDataEntity789));
		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(INTERNAL, municipalityId)).thenReturn(List.of(caseMetaDataEntity101, caseMetaDataEntity112, caseMetaDataEntity115));

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenThrow(new RuntimeException("Error"));
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);
		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("115", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);

		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId)).thenReturn(false);

		when(mockCaseMetaDataRepository.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity101))
			.thenReturn(Optional.of(caseMetaDataEntity112))
			.thenReturn(Optional.of(caseMetaDataEntity115))
			.thenReturn(Optional.of(caseMetaDataEntity123))
			.thenReturn(Optional.of(caseMetaDataEntity456))
			.thenReturn(Optional.of(caseMetaDataEntity789));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(6)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(6)).save(caseEntityCaptor.capture());
		verify(consumerMock).accept("Error while fetching errands by familyId");

		assertThat(caseEntityCaptor.getAllValues()).hasSize(6)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", caseMetaDataEntity101, PENDING, expectedFlowInstance),
					tuple("234567", caseMetaDataEntity112, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity115, PENDING, expectedFlowInstance),
					tuple("123456", caseMetaDataEntity123, PENDING, expectedFlowInstance),
					tuple("234567", caseMetaDataEntity456, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity789, PENDING, expectedFlowInstance));

	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExceptionOfOneErrandId() throws Exception {
		// Arrange
		final var municipalityId = "municipalityId";
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");

		final var flowInstanceXml = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		final var expectedFlowInstance = Base64.getEncoder().encodeToString(flowInstanceXml);

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status);

		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(EXTERNAL, municipalityId)).thenReturn(List.of(caseMetaDataEntity123));
		when(mockCaseMetaDataRepository.findByInstanceAndMunicipalityId(INTERNAL, municipalityId)).thenReturn(emptyList());

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrand("123456")).thenReturn(flowInstanceXml);
		when(mockOpenEExternalClient.getErrand("234567")).thenThrow(new RuntimeException("Error"));
		when(mockOpenEExternalClient.getErrand("345678")).thenReturn(flowInstanceXml);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);

		when(mockCaseMetaDataRepository.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity123));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(mockOpenEExternalClient).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockCaseRepository, times(3)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(mockOpenEExternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(2)).save(caseEntityCaptor.capture());
		verify(consumerMock).accept("Error while fetching errand by flowInstanceId");

		assertThat(caseEntityCaptor.getAllValues()).hasSize(2)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", caseMetaDataEntity123, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity123, PENDING, expectedFlowInstance));

	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void updateOpenECaseStatus(final Instance instance) {
		// Arrange
		final var flowInstanceId = "123";
		final var caseMetaDataEntity = CaseMetaDataEntity.create().withOpenEUpdateStatus("status").withInstance(instance);

		// Act
		openEService.updateOpenECaseStatus(flowInstanceId, caseMetaDataEntity);

		// Assert and verify
		if (EXTERNAL.equals(instance)) {
			verify(mockOpenEExternalSoapClient).setStatus(setStatusCaptor.capture());
		} else {
			verify(mockOpenEInternalSoapClient).setStatus(setStatusCaptor.capture());
		}

		assertThat(setStatusCaptor.getValue()).extracting(SetStatus::getFlowInstanceID, SetStatus::getStatusAlias).containsExactly(Integer.parseInt(flowInstanceId), "status");
		verifyNoMoreInteractions(mockOpenEExternalSoapClient, mockOpenEInternalSoapClient);
	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void confirmDelivery(final Instance instance) {
		// Arrange
		final var flowInstanceId = "123";
		final var errandId = "errandId";

		// Act
		openEService.confirmDelivery(flowInstanceId, instance, errandId);

		// Assert and verify
		if (EXTERNAL.equals(instance)) {
			verify(mockOpenEExternalSoapClient).confirmDelivery(confirmDeliveryCaptor.capture());
		} else {
			verify(mockOpenEInternalSoapClient).confirmDelivery(confirmDeliveryCaptor.capture());
		}

		assertThat(confirmDeliveryCaptor.getValue().getFlowInstanceID()).isEqualTo(Integer.parseInt(flowInstanceId));
		assertThat(confirmDeliveryCaptor.getValue().isDelivered()).isTrue();
		assertThat(confirmDeliveryCaptor.getValue().getExternalID().getSystem()).isEqualTo(SYSTEM_SUPPORT_MANAGEMENT);
		assertThat(confirmDeliveryCaptor.getValue().getExternalID().getID()).isEqualTo(errandId);

		verifyNoMoreInteractions(mockOpenEExternalSoapClient, mockOpenEInternalSoapClient);
	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void getFile(final Instance instance) {

		// Arrange
		final var externalCaseId = "externalCaseId";
		final var fileId = "fileId";
		final var queryId = "queryId";
		final var fileBytes = new byte[] {
			1, 2, 3
		};
		final var stream = new ByteArrayInputStream(fileBytes);
		final var response = Response.builder()
			.request(Request.create(Request.HttpMethod.GET, "", Map.of(), null, null, null))
			.body(stream, fileBytes.length)
			.build();
		if (EXTERNAL.equals(instance)) {
			when(mockOpenEExternalClient.getFile(externalCaseId, queryId, fileId)).thenReturn(response);
		} else {
			when(mockOpenEInternalClient.getFile(externalCaseId, queryId, fileId)).thenReturn(response);
		}

		// Act
		final var result = openEService.getFile(externalCaseId, fileId, queryId, instance);

		// Assert and verify
		assertThat(result).isEqualTo(response);

		if (EXTERNAL.equals(instance)) {
			verify(mockOpenEExternalClient).getFile(externalCaseId, queryId, fileId);
		} else {
			verify(mockOpenEInternalClient).getFile(externalCaseId, queryId, fileId);
		}
		verifyNoMoreInteractions(mockOpenEExternalClient);
	}

}
