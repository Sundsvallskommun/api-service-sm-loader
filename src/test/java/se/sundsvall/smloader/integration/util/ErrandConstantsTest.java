package se.sundsvall.smloader.integration.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class ErrandConstantsTest {
	// Test ErrandConstants

	@Test
	void errandConstants() {
		assertThat(ErrandConstants.ROLE_CONTACT_PERSON).isEqualTo("CONTACT");
		assertThat(ErrandConstants.ROLE_APPLICANT).isEqualTo("PRIMARY");
		assertThat(ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL).isEqualTo("Email");
		assertThat(ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE).isEqualTo("Phone");
		assertThat(ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE).isEqualTo("ESERVICE");
		assertThat(ErrandConstants.INTERNAL_CHANNEL_E_SERVICE).isEqualTo("ESERVICE_INTERNAL");
	}
}
