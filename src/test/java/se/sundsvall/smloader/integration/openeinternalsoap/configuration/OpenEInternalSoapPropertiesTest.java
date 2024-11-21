package se.sundsvall.smloader.integration.openeinternalsoap.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OpenEInternalSoapPropertiesTest {

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
