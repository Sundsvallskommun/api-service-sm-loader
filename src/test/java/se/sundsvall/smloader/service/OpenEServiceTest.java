package se.sundsvall.smloader.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.INTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.SYSTEM_SUPPORT_MANAGEMENT;

import feign.Request;
import feign.Response;
import generated.se.sundsvall.oepintegrator.CaseEnvelope;
import generated.se.sundsvall.oepintegrator.CaseStatusChangeRequest;
import generated.se.sundsvall.oepintegrator.ConfirmDeliveryRequest;
import generated.se.sundsvall.oepintegrator.InstanceType;
import generated.se.sundsvall.oepintegrator.ModelCase;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;
import se.sundsvall.smloader.integration.oepintegrator.OepIntegratorClient;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class OpenEServiceTest {

	@Mock
	private OepIntegratorClient oepIntegratorClientMock;

	@Mock
	private CaseRepository caseRepositoryMock;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepositoryMock;

	@Mock
	private Consumer<String> consumerMock;

	@InjectMocks
	private OpenEService openEService;

	@Captor
	private ArgumentCaptor<CaseEntity> caseEntityCaptor;

	@Captor
	private ArgumentCaptor<CaseStatusChangeRequest> setStatusCaptor;

	@Captor
	private ArgumentCaptor<ConfirmDeliveryRequest> confirmDeliveryCaptor;

	@Test
	void fetchAndSaveNewOpenECases(@Load("/open-e/flow-instance-lamna-synpunkt.xml") final String xml) throws Exception {
		// Arrange
		final var municipalityId = "municipalityId";
		final var cases = List.of(
			new CaseEnvelope().flowInstanceId("123456"),
			new CaseEnvelope().flowInstanceId("234567"),
			new CaseEnvelope().flowInstanceId("345678"));

		final var modelcase = new ModelCase().familyId("123").payload(xml);

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity456 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity789 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity112 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity115 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("115").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status).withStatsOnly(true);
		final var caseMetaDataEntity101 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status).withStatsOnly(true);

		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(EXTERNAL, municipalityId, false)).thenReturn(List.of(caseMetaDataEntity456, caseMetaDataEntity789));
		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(INTERNAL, municipalityId, false)).thenReturn(List.of(caseMetaDataEntity112, caseMetaDataEntity115));
		when(caseMetaDataRepositoryMock.findByMunicipalityIdAndStatsOnly(municipalityId, true)).thenReturn(List.of(caseMetaDataEntity101, caseMetaDataEntity123));

		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 123, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 456, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 789, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 101, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 112, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 115, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCase(anyString(), any(), anyString())).thenReturn(modelcase);
		when(oepIntegratorClientMock.getCase(anyString(), any(), anyString())).thenReturn(modelcase);

		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId)).thenReturn(false);

		when(caseMetaDataRepositoryMock.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity112))
			.thenReturn(Optional.of(caseMetaDataEntity115))
			.thenReturn(Optional.of(caseMetaDataEntity456))
			.thenReturn(Optional.of(caseMetaDataEntity789));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(oepIntegratorClientMock, times(3)).getCases(anyString(), eq(InstanceType.EXTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(oepIntegratorClientMock, times(3)).getCases(anyString(), eq(InstanceType.INTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(oepIntegratorClientMock, times(3)).getCase(anyString(), eq(InstanceType.EXTERNAL), anyString());
		verify(oepIntegratorClientMock, times(3)).getCase(anyString(), eq(InstanceType.INTERNAL), anyString());
		verify(caseRepositoryMock, times(12)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(caseRepositoryMock, times(12)).save(caseEntityCaptor.capture());
		verifyNoInteractions(consumerMock);

		assertThat(caseEntityCaptor.getAllValues()).hasSize(12)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactlyInAnyOrder(tuple("123456", caseMetaDataEntity101, PENDING, null),
					tuple("234567", caseMetaDataEntity101, PENDING, null),
					tuple("345678", caseMetaDataEntity101, PENDING, null),
					tuple("123456", caseMetaDataEntity123, PENDING, null),
					tuple("234567", caseMetaDataEntity123, PENDING, null),
					tuple("345678", caseMetaDataEntity123, PENDING, null),
					tuple("123456", caseMetaDataEntity112, PENDING, xml),
					tuple("234567", caseMetaDataEntity115, PENDING, xml),
					tuple("345678", caseMetaDataEntity456, PENDING, xml),
					tuple("123456", caseMetaDataEntity789, PENDING, xml),
					tuple("234567", caseMetaDataEntity789, PENDING, xml),
					tuple("345678", caseMetaDataEntity789, PENDING, xml));
	}

	@Test
	void fetchAndSaveStatsOnlyCases() {
		// Arrange
		final var municipalityId = "municipalityId";

		final var cases = List.of(
			new CaseEnvelope().flowInstanceId("123456"),
			new CaseEnvelope().flowInstanceId("234567"),
			new CaseEnvelope().flowInstanceId("345678"));
		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("123").withInstance(EXTERNAL)
			.withOpenEImportStatus(status).withStatsOnly(true);
		final var caseMetaDataEntity101 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("101").withInstance(INTERNAL)
			.withOpenEImportStatus(status).withStatsOnly(true);

		when(caseMetaDataRepositoryMock.findByMunicipalityIdAndStatsOnly(municipalityId, true)).thenReturn(List.of(caseMetaDataEntity101, caseMetaDataEntity123));

		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 123, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 101, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);

		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId)).thenReturn(false);

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(oepIntegratorClientMock).getCases(anyString(), eq(InstanceType.EXTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(oepIntegratorClientMock).getCases(anyString(), eq(InstanceType.INTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(oepIntegratorClientMock, never()).getCase(anyString(), any(), anyString());
		verify(oepIntegratorClientMock, never()).getCase(anyString(), any(), anyString());
		verify(caseRepositoryMock, times(6)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(caseRepositoryMock, times(6)).save(caseEntityCaptor.capture());
		verifyNoInteractions(consumerMock);

		assertThat(caseEntityCaptor.getAllValues()).hasSize(6)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactlyInAnyOrder(tuple("123456", caseMetaDataEntity101, PENDING, null),
					tuple("234567", caseMetaDataEntity101, PENDING, null),
					tuple("345678", caseMetaDataEntity101, PENDING, null),
					tuple("123456", caseMetaDataEntity123, PENDING, null),
					tuple("234567", caseMetaDataEntity123, PENDING, null),
					tuple("345678", caseMetaDataEntity123, PENDING, null));
	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExists() {
		// Arrange
		final var cases = List.of(
			new CaseEnvelope().flowInstanceId("123456"),
			new CaseEnvelope().flowInstanceId("234567"),
			new CaseEnvelope().flowInstanceId("345678"));
		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var municipalityId = "municipalityId";

		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(EXTERNAL, municipalityId, false)).thenReturn(
			List.of(CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status),
				CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status)));
		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(INTERNAL, municipalityId, false)).thenReturn(
			List.of(CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status)));

		when(caseMetaDataRepositoryMock.findByMunicipalityIdAndStatsOnly(municipalityId, true)).thenReturn(
			List.of(CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("123").withInstance(EXTERNAL)
				.withOpenEImportStatus(status).withStatsOnly(true)));

		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 123, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 456, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 789, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);

		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 112, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);

		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId))
			.thenReturn(true);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId))
			.thenReturn(true);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId))
			.thenReturn(true);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId))
			.thenReturn(true);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId))
			.thenReturn(true);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId))
			.thenReturn(true);

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(oepIntegratorClientMock, times(3)).getCases(anyString(), eq(InstanceType.EXTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(caseRepositoryMock, times(2)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId);
		verify(caseRepositoryMock).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId);
		verify(caseRepositoryMock, times(2)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId);
		verify(caseRepositoryMock).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId);
		verify(caseRepositoryMock, times(2)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId);
		verify(caseRepositoryMock).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId);
		verify(oepIntegratorClientMock).getCases(anyString(), eq(InstanceType.INTERNAL), anyInt(), anyString(), anyString(), anyString());
		verifyNoInteractions(consumerMock);

		verifyNoMoreInteractions(oepIntegratorClientMock, caseRepositoryMock);
	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExceptionOfOneFamilyId(@Load("/open-e/flow-instance-lamna-synpunkt.xml") final String xml) {
		// Arrange
		final var municipalityId = "municipalityId";
		final var cases = List.of(
			new CaseEnvelope().flowInstanceId("123456"),
			new CaseEnvelope().flowInstanceId("234567"),
			new CaseEnvelope().flowInstanceId("345678"));

		final var modelcase = new ModelCase().familyId("161").payload(xml);

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity456 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("456").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity789 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("789").withInstance(EXTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity101 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("101").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity112 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("112").withInstance(INTERNAL).withOpenEImportStatus(status);
		final var caseMetaDataEntity115 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("115").withInstance(INTERNAL).withOpenEImportStatus(status);

		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(EXTERNAL, municipalityId, false)).thenReturn(List.of(caseMetaDataEntity123, caseMetaDataEntity456, caseMetaDataEntity789));
		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(INTERNAL, municipalityId, false)).thenReturn(List.of(caseMetaDataEntity101, caseMetaDataEntity112, caseMetaDataEntity115));

		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 123, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 456, fromDate.toString(), toDate.toString(), status)).thenThrow(new RuntimeException("Error"));
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 789, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCase(anyString(), any(), anyString())).thenReturn(modelcase);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 101, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 112, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.INTERNAL, 115, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCase(anyString(), any(), anyString())).thenReturn(modelcase);

		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", INTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", INTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", INTERNAL, municipalityId)).thenReturn(false);

		when(caseMetaDataRepositoryMock.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity101))
			.thenReturn(Optional.of(caseMetaDataEntity112))
			.thenReturn(Optional.of(caseMetaDataEntity115))
			.thenReturn(Optional.of(caseMetaDataEntity123))
			.thenReturn(Optional.of(caseMetaDataEntity456))
			.thenReturn(Optional.of(caseMetaDataEntity789));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(oepIntegratorClientMock, times(3)).getCases(anyString(), eq(InstanceType.EXTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(oepIntegratorClientMock, times(3)).getCase(anyString(), eq(InstanceType.EXTERNAL), anyString());
		verify(oepIntegratorClientMock, times(3)).getCases(anyString(), eq(InstanceType.INTERNAL), anyInt(), anyString(), anyString(), anyString());
		verify(oepIntegratorClientMock, times(3)).getCase(anyString(), eq(InstanceType.INTERNAL), anyString());
		verify(caseRepositoryMock, times(6)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(caseRepositoryMock, times(6)).save(caseEntityCaptor.capture());
		verify(consumerMock).accept("Error while fetching errands by familyId 456");

		assertThat(caseEntityCaptor.getAllValues()).hasSize(6)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", caseMetaDataEntity101, PENDING, modelcase.getPayload()),
					tuple("234567", caseMetaDataEntity112, PENDING, modelcase.getPayload()),
					tuple("345678", caseMetaDataEntity115, PENDING, modelcase.getPayload()),
					tuple("123456", caseMetaDataEntity123, PENDING, modelcase.getPayload()),
					tuple("234567", caseMetaDataEntity456, PENDING, modelcase.getPayload()),
					tuple("345678", caseMetaDataEntity789, PENDING, modelcase.getPayload()));

	}

	@Test
	void fetchAndSaveNewOpenECasesWhenExceptionOfOneErrandId() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var cases = List.of(
			new CaseEnvelope().flowInstanceId("123456"),
			new CaseEnvelope().flowInstanceId("234567"),
			new CaseEnvelope().flowInstanceId("345678"));

		final var modelCase = new ModelCase().familyId("123").payload("somePayload");

		final var fromDate = LocalDateTime.now().minusDays(1);
		final var toDate = LocalDateTime.now();
		final var status = "status";
		final var caseMetaDataEntity123 = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withFamilyId("123").withInstance(EXTERNAL).withOpenEImportStatus(status);

		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(EXTERNAL, municipalityId, false)).thenReturn(List.of(caseMetaDataEntity123));
		when(caseMetaDataRepositoryMock.findByInstanceAndMunicipalityIdAndStatsOnly(INTERNAL, municipalityId, false)).thenReturn(emptyList());

		when(oepIntegratorClientMock.getCases(municipalityId, InstanceType.EXTERNAL, 123, fromDate.toString(), toDate.toString(), status)).thenReturn(cases);
		when(oepIntegratorClientMock.getCase(municipalityId, InstanceType.EXTERNAL, "123456")).thenReturn(modelCase);
		when(oepIntegratorClientMock.getCase(municipalityId, InstanceType.EXTERNAL, "234567")).thenThrow(new RuntimeException("Error"));
		when(oepIntegratorClientMock.getCase(municipalityId, InstanceType.EXTERNAL, "345678")).thenReturn(modelCase);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("123456", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("234567", EXTERNAL, municipalityId)).thenReturn(false);
		when(caseRepositoryMock.existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId("345678", EXTERNAL, municipalityId)).thenReturn(false);

		when(caseMetaDataRepositoryMock.findById(anyString())).thenReturn(Optional.of(caseMetaDataEntity123));

		// Act
		openEService.fetchAndSaveNewOpenECases(fromDate, toDate, municipalityId, consumerMock);

		// Assert and verify
		verify(oepIntegratorClientMock).getCases(any(), any(), anyInt(), anyString(), anyString(), anyString());
		verify(caseRepositoryMock, times(3)).existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(anyString(), any(Instance.class), anyString());
		verify(oepIntegratorClientMock, times(3)).getCase(anyString(), any(), anyString());

		verify(caseRepositoryMock, times(2)).save(caseEntityCaptor.capture());
		verify(consumerMock).accept("Error while fetching errand by flowInstanceId");

		assertThat(caseEntityCaptor.getAllValues()).hasSize(2)
			.extracting(CaseEntity::getExternalCaseId,
				CaseEntity::getCaseMetaData,
				CaseEntity::getDeliveryStatus,
				CaseEntity::getOpenECase).containsExactly(tuple("123456", caseMetaDataEntity123, PENDING, modelCase.getPayload()),
					tuple("345678", caseMetaDataEntity123, PENDING, modelCase.getPayload()));

	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void updateOpenECaseStatus(final Instance instance) {
		// Arrange
		final var flowInstanceId = "123";
		final var municipalityId = "municipalityId";
		final var caseMetaDataEntity = CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withOpenEUpdateStatus("status").withInstance(instance);

		// Act
		openEService.updateOpenECaseStatus(flowInstanceId, caseMetaDataEntity);

		// Assert and verify
		if (EXTERNAL.equals(instance)) {
			verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(flowInstanceId), setStatusCaptor.capture());
		} else {
			verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.INTERNAL), eq(flowInstanceId), setStatusCaptor.capture());
		}

		assertThat(setStatusCaptor.getValue()).extracting(CaseStatusChangeRequest::getName).isEqualTo("status");
		verifyNoMoreInteractions(oepIntegratorClientMock);
	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void confirmDelivery(final Instance instance) {
		// Arrange
		final var flowInstanceId = "123";
		final var errandId = "errandId";
		final var municipalityId = "municipalityId";
		final var caseMetaDataEntity = CaseMetaDataEntity.create().withInstance(instance).withMunicipalityId(municipalityId);

		// Act
		openEService.confirmDelivery(flowInstanceId, caseMetaDataEntity, errandId);

		// Assert and verify
		if (EXTERNAL.equals(instance)) {

			verify(oepIntegratorClientMock).confirmDelivery(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(flowInstanceId), confirmDeliveryCaptor.capture());
		} else {
			verify(oepIntegratorClientMock).confirmDelivery(eq(municipalityId), eq(InstanceType.INTERNAL), eq(flowInstanceId), confirmDeliveryCaptor.capture());
		}

		assertThat(confirmDeliveryCaptor.getValue().getDelivered()).isTrue();
		assertThat(confirmDeliveryCaptor.getValue().getSystem()).isEqualTo(SYSTEM_SUPPORT_MANAGEMENT);
		assertThat(confirmDeliveryCaptor.getValue().getCaseId()).isEqualTo(errandId);

		verifyNoMoreInteractions(oepIntegratorClientMock);
	}

	@ParameterizedTest
	@EnumSource(Instance.class)
	void getFile(final Instance instance) {

		// Arrange
		final var externalCaseId = "externalCaseId";
		final var fileId = "fileId";
		final var municipalityId = "municipalityId";
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
			when(oepIntegratorClientMock.getAttachment(municipalityId, InstanceType.EXTERNAL, externalCaseId, queryId, fileId)).thenReturn(response);
		} else {
			when(oepIntegratorClientMock.getAttachment(municipalityId, InstanceType.INTERNAL, externalCaseId, queryId, fileId)).thenReturn(response);
		}

		// Act
		final var result = openEService.getFile(externalCaseId, fileId, queryId, instance, municipalityId);

		// Assert and verify
		assertThat(result).isEqualTo(response);

		if (EXTERNAL.equals(instance)) {
			verify(oepIntegratorClientMock).getAttachment(municipalityId, InstanceType.EXTERNAL, externalCaseId, queryId, fileId);
		} else {
			verify(oepIntegratorClientMock).getAttachment(municipalityId, InstanceType.INTERNAL, externalCaseId, queryId, fileId);
		}
		verifyNoMoreInteractions(oepIntegratorClientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2024-06-01", "1999-12-31", "2020-02-29"
	})
	void formatLocalDate_happyCases(final String input) {
		// Act
		final String result = openEService.formatLocalDate(input);

		// Assert
		assertThat(result).isEqualTo(input);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"not-a-date", "20240601", "31-12-1999", "2024/06/01"
	})
	void formatLocalDate_nonHappyAndEdgeCases(final String input) {
		// Act
		final String result = openEService.formatLocalDate(input);

		// Assert
		// For null input, should return null; for others, returns the input as is (since .formatted() does not parse)
		if (input == null) {
			assertThat(result).isNull();
		} else {
			assertThat(result).isEqualTo(input);
		}
	}

}
