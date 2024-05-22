package se.sundsvall.smloader.integration.openeexternalsoap.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;
import se.sundsvall.smloader.integration.openeinternalsoap.configuration.OpenEInternalSoapProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OpenEExternalSoapPropertiesTest {

	@Autowired
	private OpenEInternalSoapProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
		assertThat(properties.username()).isEqualTo("username");
		assertThat(properties.password()).isEqualTo("password");
	}

}
