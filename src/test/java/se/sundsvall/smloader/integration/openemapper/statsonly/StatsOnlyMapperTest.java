package se.sundsvall.smloader.integration.openemapper.statsonly;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Priority;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.openemapper.OpenEStatsOnlyMapperProperties;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.INTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

@ExtendWith(MockitoExtension.class)
class StatsOnlyMapperTest {

	@Mock
	private OpenEStatsOnlyMapperProperties properties;

	@InjectMocks
	private StatsOnlyMapper statsOnlyMapper;

	@Test
	void mapToErrand() {
		// Arrange
		final var serviceName = "serviceName";
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var labels = List.of("label1", "label2");
		final var caseEntity = CaseEntity.create()
			.withId("id")
			.withExternalCaseId("externalCaseId")
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withFamilyId("161")
				.withInstance(EXTERNAL)
				.withNamespace("namespace")
				.withMunicipalityId("municipalityId")
				.withStatsOnly(true))
			.withOpenECase(null)
			.withDeliveryStatus(PENDING);

		final var proposalProperties = new OpenEMapperProperties();
		proposalProperties.setServiceName(serviceName);
		proposalProperties.setPriority(priority);
		proposalProperties.setCategory(category);
		proposalProperties.setType(type);
		proposalProperties.setLabels(labels);

		when(properties.getServices()).thenReturn(Map.of("161", proposalProperties));

		// Act
		final var errand = statsOnlyMapper.mapToErrand(caseEntity, "161", INTERNAL);

		// Assert and verify
		assertThat(errand.get().getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.get().getTitle()).isEqualTo(serviceName);
		assertThat(errand.get().getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.get().getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.get().getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.get().getBusinessRelated()).isFalse();
		assertThat(errand.get().getParameters()).isEmpty();

		assertThat(errand.get().getStakeholders()).isEmpty();
		assertThat(errand.get().getExternalTags()).containsExactlyInAnyOrder(
			new ExternalTag().key("caseId").value("externalCaseId"));
		assertThat(errand.get().getReporterUserId()).isEqualTo("unknown");

		verify(properties, times(2)).getServices();
		verifyNoMoreInteractions(properties);
	}

	@Test
	void mapToErrandWhenNoConfig() {
		// Arrange
		final var caseEntity = CaseEntity.create()
			.withId("id")
			.withExternalCaseId("externalCaseId")
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withFamilyId("161")
				.withInstance(EXTERNAL)
				.withNamespace("namespace")
				.withMunicipalityId("municipalityId")
				.withStatsOnly(true))
			.withOpenECase(null)
			.withDeliveryStatus(PENDING);

		when(properties.getServices()).thenReturn(emptyMap());

		// Act
		final var errand = statsOnlyMapper.mapToErrand(caseEntity, "161", INTERNAL);

		// Assert and verify
		assertThat(errand).isEmpty();
		verify(properties).getServices();
		verifyNoMoreInteractions(properties);
	}

	@Test
	void mapToErrandWhenException() {
		// Arrange
		final var caseEntity = CaseEntity.create()
			.withId("id")
			.withExternalCaseId("externalCaseId")
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withFamilyId("161")
				.withInstance(EXTERNAL)
				.withNamespace("namespace")
				.withMunicipalityId("municipalityId")
				.withStatsOnly(true))
			.withOpenECase(null)
			.withDeliveryStatus(PENDING);

		when(properties.getServices()).thenThrow(new RuntimeException());

		// Act
		final var errand = statsOnlyMapper.mapToErrand(caseEntity, "161", INTERNAL);

		// Assert and verify
		assertThat(errand).isEmpty();
		verify(properties).getServices();
		verifyNoMoreInteractions(properties);
	}

}
