package se.sundsvall.smloader.integration.openemapper.providefeedback;

import generated.se.sundsvall.supportmanagement.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CATEGORY_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TYPE_OTHER;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ProvideFeedbackMapperTest {

	@Autowired
	private ProvideFeedbackMapper mapper;

	@Test
	void getSupportedFamilyId() {
		assertThat(mapper.getSupportedFamilyId()).isEqualTo("123");
	}

	@Test
	void mapToErrand() throws Exception {
		var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt.xml");
		var errand = mapper.mapToErrand(stringBytes);

		assertThat(errand.getDescription()).isEqualTo("beskriver synpunkten här");
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getStakeholders().getFirst().getRole()).isEqualTo(ROLE_CONTACT_PERSON);
		assertThat(errand.getStakeholders().getFirst().getFirstName()).isEqualTo("Kalle");
		assertThat(errand.getStakeholders().getFirst().getLastName()).isEqualTo("Anka");
		assertThat(errand.getStakeholders().getFirst().getContactChannels()).hasSize(2);
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getFirst().getType()).isEqualTo("Email");
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getFirst().getValue()).isEqualTo("kalle.anka@sundsvall.se");
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getLast().getType()).isEqualTo("Phone");
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getLast().getValue()).isEqualTo("070111222");
		assertThat(errand.getClassification().getType()).isEqualTo(TYPE_OTHER);
		assertThat(errand.getClassification().getCategory()).isEqualTo(CATEGORY_LAMNA_SYNPUNKT);
		assertThat(errand.getPriority()).isEqualTo(Priority.LOW);
		assertThat(errand.getTitle()).isEqualTo("testar");
		assertThat(errand.getStatus()).isEqualTo("NEW");
		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");
	}

	@Test
	void mapToAnonymousErrand() throws Exception {
		var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt-anonymous.xml");
		var errand = mapper.mapToErrand(stringBytes);

		assertThat(errand.getDescription()).isEqualTo("testar synen");
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getClassification().getCategory()).isEqualTo(CATEGORY_LAMNA_SYNPUNKT);
		assertThat(errand.getClassification().getType()).isEqualTo(TYPE_OTHER);
		assertThat(errand.getStakeholders()).isEmpty();
		assertThat(errand.getPriority()).isEqualTo(Priority.LOW);
		assertThat(errand.getTitle()).isEqualTo("testsyn");
		assertThat(errand.getStatus()).isEqualTo("NEW");
		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");
	}
}
