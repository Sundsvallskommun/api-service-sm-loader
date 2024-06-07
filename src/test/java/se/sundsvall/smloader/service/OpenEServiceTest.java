package se.sundsvall.smloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.Instance;
import se.sundsvall.smloader.integration.openeexternal.OpenEExternalClient;
import se.sundsvall.smloader.integration.openeinternal.OpenEInternalClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.db.model.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.db.model.Instance.INTERNAL;

@ExtendWith(MockitoExtension.class)
class OpenEServiceTest {

	@Mock
	private OpenEExternalClient mockOpenEExternalClient;

	@Mock
	private OpenEInternalClient mockOpenEInternalClient;

	@Mock
	private CaseRepository mockCaseRepository;

	@InjectMocks
	private OpenEService openEService;

	@Captor
	private ArgumentCaptor<CaseEntity> caseEntityCaptor;

	@Test
	void fetchAndSaveNewOpenECases() throws Exception {
		// Arrange
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");

		final var flowInstanceXml = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";

		ReflectionTestUtils.setField(openEService, "externalFamilyIds", "123,456,789");
		ReflectionTestUtils.setField(openEService, "internalFamilyIds", "101,112");

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);
		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrand(anyString())).thenReturn(flowInstanceXml);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("123456", EXTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("234567", EXTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("345678", EXTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("123456", INTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("234567", INTERNAL)).thenReturn(false);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("345678", INTERNAL)).thenReturn(false);



		// Act
		openEService.fetchAndSaveNewOpenECases(status, fromDate, toDate);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());
		verify(mockCaseRepository, times(6)).existsByOpenECaseIdAndInstance(anyString(), any(Instance.class));

		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockOpenEInternalClient, times(3)).getErrand(anyString());

		verify(mockCaseRepository, times(6)).save(caseEntityCaptor.capture());

		assertThat( caseEntityCaptor.getAllValues()).hasSize(6)
			.extracting(CaseEntity::getOpenECaseId,
				CaseEntity::getInstance,
				CaseEntity::getFamilyId,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", EXTERNAL, "161", PENDING, new String(flowInstanceXml)),
					tuple("234567", EXTERNAL, "161", PENDING, new String(flowInstanceXml)),
					tuple("345678", EXTERNAL, "161", PENDING, new String(flowInstanceXml)),
					tuple("123456", INTERNAL, "161", PENDING, new String(flowInstanceXml)),
					tuple("234567", INTERNAL, "161", PENDING, new String(flowInstanceXml)),
					tuple("345678", INTERNAL, "161", PENDING, new String(flowInstanceXml)));
	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExists() throws Exception {
		// Arrange
		final var flowInstancesXml = readOpenEFile("flow-instances.xml");
		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";

		ReflectionTestUtils.setField(openEService, "externalFamilyIds", "123,456,789");
		ReflectionTestUtils.setField(openEService, "internalFamilyIds", "101,112");

		when(mockOpenEExternalClient.getErrandIds("123", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("456", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEExternalClient.getErrandIds("789", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);

		when(mockOpenEInternalClient.getErrandIds("101", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);
		when(mockOpenEInternalClient.getErrandIds("112", status, fromDate.toString(), toDate.toString())).thenReturn(flowInstancesXml);

		when(mockCaseRepository.existsByOpenECaseIdAndInstance("123456", EXTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("234567", EXTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("345678", EXTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("123456", INTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("234567", INTERNAL)).thenReturn(true);
		when(mockCaseRepository.existsByOpenECaseIdAndInstance("345678", INTERNAL)).thenReturn(true);



		// Act
		openEService.fetchAndSaveNewOpenECases(status, fromDate, toDate);

		// Assert and verify
		verify(mockOpenEExternalClient, times(3)).getErrandIds(anyString(), anyString(), anyString(), anyString());
		verify(mockCaseRepository).existsByOpenECaseIdAndInstance("123456", EXTERNAL);
		verify(mockCaseRepository).existsByOpenECaseIdAndInstance("123456", INTERNAL);
		verify(mockCaseRepository).existsByOpenECaseIdAndInstance("234567", EXTERNAL);
		verify(mockCaseRepository).existsByOpenECaseIdAndInstance("234567", INTERNAL);
		verify(mockCaseRepository).existsByOpenECaseIdAndInstance("345678", EXTERNAL);
		verify(mockCaseRepository).existsByOpenECaseIdAndInstance("345678", INTERNAL);

		verify(mockOpenEInternalClient, times(2)).getErrandIds(anyString(), anyString(), anyString(), anyString());

		verifyNoMoreInteractions(mockOpenEExternalClient, mockOpenEInternalClient, mockCaseRepository);
	}
}
