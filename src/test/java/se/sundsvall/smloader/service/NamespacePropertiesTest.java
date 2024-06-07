package se.sundsvall.smloader.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class NamespacePropertiesTest {

	@Autowired
	private NamespaceProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.getNamespace()).containsEntry("CONTACTCENTER", List.of("123", "456","161"));
	}

}
