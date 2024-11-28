package se.sundsvall.smloader.integration.openeinternal.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OpenEInternalPropertiesTest {

	@Autowired
	private OpenEInternalProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.username()).isEqualTo("username");
		assertThat(properties.password()).isEqualTo("password");
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
	}

}
