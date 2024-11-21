package se.sundsvall.smloader.integration.openemapper.providefeedback;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;

import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

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
		final var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt.xml");
		final var errand = mapper.mapToErrand(stringBytes);

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
		assertThat(errand.getClassification().getType()).isEqualTo("FEEDBACK_TYPE");
		assertThat(errand.getClassification().getCategory()).isEqualTo("FEEDBACK_CATEGORY");
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getTitle()).isEqualTo("testar");
		assertThat(errand.getStatus()).isEqualTo("NEW");
		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("4164"),
			new ExternalTag().key("familyId").value("161")));
	}

	@Test
	void mapToAnonymousErrand() throws Exception {
		final var stringBytes = readOpenEFile("flow-instance-lamna-synpunkt-anonymous.xml");
		final var errand = mapper.mapToErrand(stringBytes);

		assertThat(errand.getDescription()).isEqualTo("testar synen");
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getClassification().getCategory()).isEqualTo("FEEDBACK_CATEGORY");
		assertThat(errand.getClassification().getType()).isEqualTo("FEEDBACK_TYPE");
		assertThat(errand.getStakeholders()).isEmpty();
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getTitle()).isEqualTo("testsyn");
		assertThat(errand.getStatus()).isEqualTo("NEW");
		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("4179"),
			new ExternalTag().key("familyId").value("161")));
	}
}
