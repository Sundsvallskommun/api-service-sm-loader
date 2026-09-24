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

	@Autowired
	@Qualifier("facilityorder")
	private OpenEMapperProperties propertiesFacilityOrder;

	@Test
	void testTwentyFiveAtWorkProperties() {
		assertThat(propertiesFeedback.getFamilyId()).isEqualTo("131");
		assertThat(propertiesFeedback.getPriority()).isEqualTo("MEDIUM");
		assertThat(propertiesFeedback.getCategory()).isEqualTo("SALARY");
		assertThat(propertiesFeedback.getType()).isEqualTo("SALARY.OTHER");
		assertThat(propertiesFeedback.getLabels()).hasSize(3).containsExactly("SALARY", "SALARY.OTHER", "SALARY.OTHER.TWENTY_FIVE_YEARS_GIFT");
	}

	@Test
	void testFacilityOrderProperties() {
		assertThat(propertiesFacilityOrder.getFamilyId()).isEqualTo("250");
		assertThat(propertiesFacilityOrder.getPriority()).isEqualTo("MEDIUM");
		assertThat(propertiesFacilityOrder.getCategory()).isEqualTo("E_POSTARENDE");
		assertThat(propertiesFacilityOrder.getType()).isEqualTo("E_POSTARENDE.UNCATEGORIZED");
		assertThat(propertiesFacilityOrder.getLabels()).hasSize(2).containsExactly("E_POSTARENDE", "E_POSTARENDE/UNCATEGORIZED");
	}
}
