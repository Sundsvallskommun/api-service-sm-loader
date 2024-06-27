package se.sundsvall.smloader.integration.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ErrandConstantsTest {
	//Test ErrandConstants

	@Test
	void errandConstants() {
		assertThat(ErrandConstants.ROLE_CONTACT_PERSON).isEqualTo("CONTACT_PERSON");
		assertThat(ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL).isEqualTo("Email");
		assertThat(ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE).isEqualTo("Phone");
		assertThat(ErrandConstants.CATEGORY_LAMNA_SYNPUNKT).isEqualTo("LAMNA_SYNPUNKT");
		assertThat(ErrandConstants.CATEGORY_SUNDSVALLS_FORSLAGET).isEqualTo("SUNDSVALLS_FORSLAGET");
		assertThat(ErrandConstants.TYPE_OTHER).isEqualTo("OTHER");
		assertThat(ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE).isEqualTo("ESERVICE");
		assertThat(ErrandConstants.INTERNAL_CHANNEL_E_SERVICE).isEqualTo("ESERVICE_INTERNAL");
	}
}
