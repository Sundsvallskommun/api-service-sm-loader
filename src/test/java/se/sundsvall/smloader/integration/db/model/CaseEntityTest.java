package se.sundsvall.smloader.integration.db.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class CaseEntityTest {

	@Test
	void testBean() {
		assertThat(CaseEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void hasValidBuilderMethods() {

		final var id = "id";
		final var familyId = "familyId";
		final var externalCaseId = "externalCaseId";
		final var instance = Instance.EXTERNAL;
		final var openECase = "openECase";
		final var deliveryStatus = DeliveryStatus.CREATED;
		final var namespace = "namespace";
		final var caseMetaDataEntity = CaseMetaDataEntity.create()
			.withFamilyId(familyId)
			.withInstance(instance)
			.withNamespace(namespace);

		final var entity = CaseEntity.create()
			.withId(id)
			.withCaseMetaData(caseMetaDataEntity)
			.withExternalCaseId(externalCaseId)
			.withOpenECase(openECase)
			.withDeliveryStatus(deliveryStatus);

		Assertions.assertThat(entity).hasNoNullFieldsOrProperties();
		Assertions.assertThat(entity.getId()).isEqualTo(id);
		Assertions.assertThat(entity.getCaseMetaData()).isEqualTo(caseMetaDataEntity);
		Assertions.assertThat(entity.getExternalCaseId()).isEqualTo(externalCaseId);
		Assertions.assertThat(entity.getDeliveryStatus()).isEqualTo(deliveryStatus);
		Assertions.assertThat(entity.getOpenECase()).isEqualTo(openECase);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(CaseEntity.create()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new CaseEntity()).hasAllNullFieldsOrProperties();
	}
}
