package se.sundsvall.smloader.integration.openemapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OpenEMapperConfigurationTest {

	@Autowired
	@Qualifier("twentyfiveatwork")
	private OpenEMapperProperties propertiesFeedback;

	@Test
	void testTwentyFiveAtWorkProperties() {
		assertThat(propertiesFeedback.getFamilyId()).isEqualTo("131");
		assertThat(propertiesFeedback.getPriority()).isEqualTo("MEDIUM");
		assertThat(propertiesFeedback.getCategory()).isEqualTo("SALARY");
		assertThat(propertiesFeedback.getType()).isEqualTo("SALARY.OTHER");
		assertThat(propertiesFeedback.getLabels()).hasSize(3).containsExactly("SALARY", "SALARY.OTHER", "SALARY.OTHER.TWENTY_FIVE_YEARS_GIFT");
	}
}
