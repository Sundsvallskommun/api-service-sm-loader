package se.sundsvall.smloader.integration.openemapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OpenEMapperConfigurationTest {

	@Autowired
	@Qualifier("feedback")
	private OpenEMapperProperties propertiesFeedback;

	@Autowired
	@Qualifier("proposal")
	private OpenEMapperProperties propertiesProposal;

	@Test
	void testFeedbackProperties() {
		assertThat(propertiesFeedback.getFamilyId()).isEqualTo("123");
		assertThat(propertiesFeedback.getPriority()).isEqualTo("MEDIUM");
		assertThat(propertiesFeedback.getCategory()).isEqualTo("FEEDBACK_CATEGORY");
		assertThat(propertiesFeedback.getType()).isEqualTo("FEEDBACK_TYPE");
	}

	@Test
	void testProposalProperties() {
		assertThat(propertiesProposal.getFamilyId()).isEqualTo("456");
		assertThat(propertiesProposal.getPriority()).isEqualTo("MEDIUM");
		assertThat(propertiesProposal.getCategory()).isEqualTo("PROPOSAL_CATEGORY");
		assertThat(propertiesProposal.getType()).isEqualTo("PROPOSAL_TYPE");
	}
}
