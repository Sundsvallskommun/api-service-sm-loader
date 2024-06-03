package se.sundsvall.smloader.integration.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ErrandConstantsTest {
	//Test ErrandConstants

	@Test
	void errandConstants() {
		assertThat(ErrandConstants.MUNICIPALITY_ID).isEqualTo("2281");
		assertThat(ErrandConstants.NAMESPACE_BY_FAMILY_ID.get("161")).contains("CONTACTCENTER");
		assertThat(ErrandConstants.NAMESPACE_BY_FAMILY_ID.get("77")).contains("CONTACTCENTER");
		assertThat(ErrandConstants.ROLE_CONTACT_PERSON).isEqualTo("CONTACT_PERSON");
		assertThat(ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL).isEqualTo("Email");
		assertThat(ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE).isEqualTo("Phone");
		assertThat(ErrandConstants.CATEGORY_LAMNA_SYNPUNKT).isEqualTo("LAMNA_SYNPUNKT");
	}
}
