package se.sundsvall.smloader.integration.openemapper;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AttachmentTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Attachment.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";

		// Act
		final var result = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getFileId()).isEqualTo(fileId);
		assertThat(result.getFileName()).isEqualTo(fileName);
		assertThat(result.getQueryId()).isEqualTo(queryId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Attachment.create()).hasAllNullFieldsOrProperties();
		assertThat(new Attachment()).hasAllNullFieldsOrProperties();
	}

}
