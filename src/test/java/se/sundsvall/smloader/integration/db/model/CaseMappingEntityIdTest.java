package se.sundsvall.smloader.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CaseMappingEntityIdTest {

	@Test
	void testBean() {
		assertThat(CaseMappingId.class, allOf(
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
		final var entity = CaseMappingId.create()
			.withErrandId(errandId)
			.withExternalCaseId(externalCaseId);

		Assertions.assertThat(entity).hasNoNullFieldsOrProperties();
		Assertions.assertThat(entity.getErrandId()).isEqualTo(errandId);
		Assertions.assertThat(entity.getExternalCaseId()).isEqualTo(externalCaseId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(CaseMappingId.create()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new CaseMappingId()).hasAllNullFieldsOrProperties();
	}
}
