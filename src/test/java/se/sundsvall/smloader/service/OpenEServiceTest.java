package se.sundsvall.smloader.service;

import generated.se.sundsvall.callback.ConfirmDelivery;
import generated.se.sundsvall.callback.SetStatus;
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

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.INTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.SYSTEM_SUPPORT_MANAGEMENT;

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
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");

		final var flowInstanceXml = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		final var expectedFlowInstance = Base64.getEncoder().encodeToString(flowInstanceXml);

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity_123 = CaseMetaDataEntity.create().withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity_456 = CaseMetaDataEntity.create().withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity_789 = CaseMetaDataEntity.create().withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity_101 = CaseMetaDataEntity.create().withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity_112 = CaseMetaDataEntity.create().withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity_115 = CaseMetaDataEntity.create().withFamilyId("115").withInstance(INTERNAL).withOpenEImportStatus(status);

		when(mockCaseMetaDataRepository.findByInstance(EXTERNAL)).thenReturn(List.of(caseMetaDataEntity_123, caseMetaDataEntity_456, caseMetaDataEntity_789));

		when(mockCaseMetaDataRepository.findByInstance(INTERNAL)).thenReturn(List.of(caseMetaDataEntity_101, caseMetaDataEntity_112, caseMetaDataEntity_115));

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);
		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("115", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);

		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("123456", EXTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("234567", EXTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("345678", EXTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("123456", INTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("234567", INTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("345678", INTERNAL)).thenReturn(false);

		when(mockCaseMetaDataRepository.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity_101))
			.thenReturn(Optional.of(caseMetaDataEntity_112))
			.thenReturn(Optional.of(caseMetaDataEntity_115))
			.thenReturn(Optional.of(caseMetaDataEntity_123))
			.thenReturn(Optional.of(caseMetaDataEntity_456))
			.thenReturn(Optional.of(caseMetaDataEntity_789));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(6)).existsByExternalCaseIdAndCaseMetaDataEntityInstance(anyString(), any(Instance.class));

		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());

		verify(mockCaseRepository, times(6)).save(caseEntityCaptor.capture());

		assertThat( caseEntityCaptor.getAllValues()).hasSize(6)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", caseMetaDataEntity_101, PENDING, expectedFlowInstance),
					tuple("234567", caseMetaDataEntity_112, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity_115, PENDING, expectedFlowInstance),
					tuple("123456", caseMetaDataEntity_123, PENDING, expectedFlowInstance),
					tuple("234567", caseMetaDataEntity_456, PENDING, expectedFlowInstance),
					tuple("345678", caseMetaDataEntity_789, PENDING, expectedFlowInstance));
	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExists() throws Exception {
		// Arrange
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");
		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";

		when(mockCaseMetaDataRepository.findByInstance(EXTERNAL)).thenReturn(List.of(CaseMetaDataEntity.create().withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status),
			CaseMetaDataEntity.create().withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status),
			CaseMetaDataEntity.create().withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status)));
		when(mockCaseMetaDataRepository.findByInstance(INTERNAL)).thenReturn(List.of(CaseMetaDataEntity.create().withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status),
			CaseMetaDataEntity.create().withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status)));

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);

		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);

		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("123456", EXTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("234567", EXTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("345678", EXTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("123456", INTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("234567", INTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByExternalCaseIdAndCaseMetaDataEntityInstance("345678", INTERNAL)).thenReturn(true);



		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstance("123456", EXTERNAL);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstance("123456", INTERNAL);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstance("234567", EXTERNAL);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstance("234567", INTERNAL);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstance("345678", EXTERNAL);
		verify(mockCaseRepository).existsByExternalCaseIdAndCaseMetaDataEntityInstance("345678", INTERNAL);

		verify(mockOpenEInternalClient, times(2)).getErrandIds(anyString(), anyString(), anyString(), anyString());

		verifyNoMoreInteractions(mockOpenEExternalClient, mockOpenEInternalClient, mockCaseRepository);
	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void updateOpenECaseStatus(Instance instance) {
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
	void confirmDelivery(Instance instance) {
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

}
