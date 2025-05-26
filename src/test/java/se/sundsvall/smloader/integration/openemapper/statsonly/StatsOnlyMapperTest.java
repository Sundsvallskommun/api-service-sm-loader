package se.sundsvall.smloader.integration.openemapper.statsonly;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.INTERNAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

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
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.openemapper.OpenEStatsOnlyMapperProperties;

@ExtendWith(MockitoExtension.class)
class StatsOnlyMapperTest {

	@Mock
	private OpenEStatsOnlyMapperProperties properties;

	@InjectMocks
	private StatsOnlyMapper statsOnlyMapper;

	@Test
	void mapToErrand() throws Exception {
		// Arrange
		final var serviceName = "serviceName";
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var labels = List.of("label1", "label2");
		final var proposalProperties = new OpenEMapperProperties();
		proposalProperties.setServiceName(serviceName);
		proposalProperties.setPriority(priority);
		proposalProperties.setCategory(category);
		proposalProperties.setType(type);
		proposalProperties.setLabels(labels);

		when(properties.getServices()).thenReturn(Map.of("161", proposalProperties));

		final var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		// Act
		final var errand = statsOnlyMapper.mapToErrand(stringBytes, "161", INTERNAL);

		// Assert and verify
		assertThat(errand.get().getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.get().getTitle()).isEqualTo(serviceName);
		assertThat(errand.get().getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.get().getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.get().getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.get().getBusinessRelated()).isFalse();
		assertThat(errand.get().getParameters()).isEmpty();

		assertThat(errand.get().getLabels()).hasSize(2).containsExactly("label1", "label2");

		assertThat(errand.get().getStakeholders()).isEmpty();
		assertThat(errand.get().getExternalTags()).containsExactlyInAnyOrder(
			new ExternalTag().key("caseId").value("4164"));
		assertThat(errand.get().getReporterUserId()).isEqualTo("unknown");

		verify(properties, times(2)).getServices();
		verifyNoMoreInteractions(properties);
	}

	@Test
	void mapToErrandWhenNoConfig() throws Exception {
		// Arrange
		when(properties.getServices()).thenReturn(emptyMap());

		final var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		// Act
		final var errand = statsOnlyMapper.mapToErrand(stringBytes, "161", INTERNAL);

		// Assert and verify
		assertThat(errand).isEmpty();
		verify(properties).getServices();
		verifyNoMoreInteractions(properties);
	}

	@Test
	void mapToErrandWhenException() throws Exception {
		// Arrange
		when(properties.getServices()).thenThrow(new RuntimeException());

		final var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		// Act
		final var errand = statsOnlyMapper.mapToErrand(stringBytes, "161", INTERNAL);

		// Assert and verify
		assertThat(errand).isEmpty();
		verify(properties).getServices();
		verifyNoMoreInteractions(properties);
	}

}
