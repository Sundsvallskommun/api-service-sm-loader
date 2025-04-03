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
import se.sundsvall.smloader.integration.db.model.enums.Instance;

class CaseMetaDataEntityTest {

	@Test
	void testBean() {
		assertThat(CaseMetaDataEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void hasValidBuilderMethods() {

		final var familyId = "familyId";
		final var instance = Instance.EXTERNAL;
		final var openEUpdateStatus = "openEUpdateStatus";
		final var openEImportStatus = "openEImportStatus";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var statsOnly = true;

		final var entity = CaseMetaDataEntity.create()
			.withFamilyId(familyId)
			.withInstance(instance)
			.withOpenEUpdateStatus(openEUpdateStatus)
			.withOpenEImportStatus(openEImportStatus)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withStatsOnly(statsOnly);

		Assertions.assertThat(entity).hasNoNullFieldsOrProperties();
		Assertions.assertThat(entity.getFamilyId()).isEqualTo(familyId);
		Assertions.assertThat(entity.getInstance()).isEqualTo(instance);
		Assertions.assertThat(entity.getOpenEUpdateStatus()).isEqualTo(openEUpdateStatus);
		Assertions.assertThat(entity.getOpenEImportStatus()).isEqualTo(openEImportStatus);
		Assertions.assertThat(entity.getNamespace()).isEqualTo(namespace);
		Assertions.assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		Assertions.assertThat(entity.isStatsOnly()).isEqualTo(statsOnly);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(CaseMetaDataEntity.create()).hasAllNullFieldsOrPropertiesExcept("statsOnly");
		Assertions.assertThat(new CaseEntity()).hasAllNullFieldsOrPropertiesExcept("statsOnly");
	}
}
