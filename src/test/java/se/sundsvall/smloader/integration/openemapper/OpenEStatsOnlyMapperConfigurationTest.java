package se.sundsvall.smloader.integration.openemapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OpenEStatsOnlyMapperConfigurationTest {

	@Autowired
	private OpenEStatsOnlyMapperProperties statsOnlyProperties;

	@Test
	void testStatsOnlyProperties() {

		assertThat(statsOnlyProperties.getServices()).hasSize(2).containsKeys("123", "456");
		assertThat(statsOnlyProperties.getServices().get("123").getServiceName()).isEqualTo("Lämna synpunkt");
		assertThat(statsOnlyProperties.getServices().get("123").getPriority()).isEqualTo("MEDIUM");
		assertThat(statsOnlyProperties.getServices().get("123").getCategory()).isEqualTo("FEEDBACK_CATEGORY");
		assertThat(statsOnlyProperties.getServices().get("123").getType()).isEqualTo("FEEDBACK_TYPE");
		assertThat(statsOnlyProperties.getServices().get("123").getLabels()).hasSize(2).containsExactly("LABEL1", "LABEL2");
		assertThat(statsOnlyProperties.getServices().get("456").getPriority()).isEqualTo("MEDIUM");
		assertThat(statsOnlyProperties.getServices().get("456").getCategory()).isEqualTo("PROPOSAL_CATEGORY");
		assertThat(statsOnlyProperties.getServices().get("456").getType()).isEqualTo("PROPOSAL_TYPE");
	}
}
