package se.sundsvall.smloader.integration.db.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class CaseMappingTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}
	@Test
	void testBean() {
		assertThat(CaseMapping.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void hasValidBuilderMethods() {

		final var errandId = "errandId";
		final var externalCaseId = "externalCaseId";
		final var caseType = "caseType";
		final var timestamp = OffsetDateTime.now();
		final var entity = CaseMapping.create()
			.withErrandId(errandId)
			.withExternalCaseId(externalCaseId)
			.withCaseType(caseType)
			.withTimestamp(timestamp);

		Assertions.assertThat(entity).hasNoNullFieldsOrProperties();
		Assertions.assertThat(entity.getErrandId()).isEqualTo(errandId);
		Assertions.assertThat(entity.getExternalCaseId()).isEqualTo(externalCaseId);
		Assertions.assertThat(entity.getTimestamp()).isEqualTo(timestamp);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(CaseMapping.create()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new CaseMapping()).hasAllNullFieldsOrProperties();
	}
}
