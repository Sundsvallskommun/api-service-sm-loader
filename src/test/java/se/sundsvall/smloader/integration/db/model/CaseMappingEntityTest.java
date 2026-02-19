package se.sundsvall.smloader.integration.db.model;

import java.time.OffsetDateTime;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class CaseMappingEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(CaseMappingEntity.class, allOf(
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
		final var modified = OffsetDateTime.now();
		final var municipalityId = "municipalityId";
		final var entity = CaseMappingEntity.create()
			.withErrandId(errandId)
			.withExternalCaseId(externalCaseId)
			.withCaseType(caseType)
			.withModified(modified)
			.withMunicipalityId(municipalityId);

		Assertions.assertThat(entity).hasNoNullFieldsOrProperties();
		Assertions.assertThat(entity.getErrandId()).isEqualTo(errandId);
		Assertions.assertThat(entity.getExternalCaseId()).isEqualTo(externalCaseId);
		Assertions.assertThat(entity.getModified()).isEqualTo(modified);
		Assertions.assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(CaseMappingEntity.create()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new CaseMappingEntity()).hasAllNullFieldsOrProperties();
	}
}
