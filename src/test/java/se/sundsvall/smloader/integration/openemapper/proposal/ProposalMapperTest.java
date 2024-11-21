package se.sundsvall.smloader.integration.openemapper.proposal;

import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ProposalMapperTest {

	@Autowired
	private ProposalMapper mapper;

	@Test
	void getSupportedFamilyId() {
		assertThat(mapper.getSupportedFamilyId()).isEqualTo("456");
	}

	@Test
	void mapToErrand() throws Exception {
		var stringBytes = readOpenEFile("flow-instance-sundsvallsforslaget.xml");
		var errand = mapper.mapToErrand(stringBytes);

		assertThat(errand.getDescription()).isEqualTo("Testar att lämna förslag");
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getStakeholders().getFirst().getRole()).isEqualTo(ROLE_CONTACT_PERSON);
		assertThat(errand.getStakeholders().getFirst().getFirstName()).isEqualTo("Kalle");
		assertThat(errand.getStakeholders().getFirst().getLastName()).isEqualTo("Anka");
		assertThat(errand.getStakeholders().getFirst().getContactChannels()).hasSize(2);
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getFirst().getType()).isEqualTo("Email");
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getFirst().getValue()).isEqualTo("kalle.anka@sundsvall.se");
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getLast().getType()).isEqualTo("Phone");
		assertThat(errand.getStakeholders().getFirst().getContactChannels().getLast().getValue()).isEqualTo("0701112223");
		assertThat(errand.getStakeholders().getFirst().getAddress()).isEqualTo("STORGATAN 1");
		assertThat(errand.getStakeholders().getFirst().getZipCode()).isEqualTo("123 45");

		assertThat(errand.getClassification().getType()).isEqualTo("PROPOSAL_TYPE");
		assertThat(errand.getClassification().getCategory()).isEqualTo("PROPOSAL_CATEGORY");
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getTitle()).isEqualTo("Testing");
		assertThat(errand.getStatus()).isEqualTo("NEW");
		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("4234"),
			new ExternalTag().key("familyId").value("370")));
	}
}
