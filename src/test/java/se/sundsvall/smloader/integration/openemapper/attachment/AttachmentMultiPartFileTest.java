package se.sundsvall.smloader.integration.openemapper.attachment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiremock.org.apache.commons.io.FileUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AttachmentMultiPartFileTest {

	@Mock
	private File fileMock;

	@Test
	void fromAttachmentWithContent() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var content = "content".getBytes();
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		// Act
		final var multipartFile = AttachmentMultiPartFile.create(attachment, content);

		// Assert
		assertThat(multipartFile.getBytes()).isEqualTo(content);
		assertThat(multipartFile.getContentType()).isEmpty();
		assertThat(multipartFile.getInputStream().readAllBytes()).isEqualTo(content);
		assertThat(multipartFile.getName()).isEqualTo(fileName);
		assertThat(multipartFile.getOriginalFilename()).isEqualTo(fileName);
		assertThat(multipartFile.getSize()).isEqualTo(content.length);
		assertThat(multipartFile.isEmpty()).isFalse();
	}

	@Test
	void fromAttachmentWithEmptyContent() throws Exception {

		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var content = "";
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		// Act
		final var multipartFile = AttachmentMultiPartFile.create(attachment, content.getBytes());

		// Assert
		assertThat(multipartFile.getBytes()).isNullOrEmpty();
		assertThat(multipartFile.getContentType()).isEmpty();
		assertThat(multipartFile.getInputStream().readAllBytes()).isEmpty();
		assertThat(multipartFile.getName()).isEqualTo(fileName);
		assertThat(multipartFile.getOriginalFilename()).isEqualTo(fileName);
		assertThat(multipartFile.getSize()).isZero();
		assertThat(multipartFile.isEmpty()).isTrue();
	}

	@Test
	void fromAttachmentWithoutContent() {
		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		// Act & Assert
		assertThrows(NullPointerException.class, () -> AttachmentMultiPartFile.create(attachment, null), "Content must be provided");
	}

	@Test
	void transferToForAttachmentWithContent() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var content = "content".getBytes();
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);
		final var multipartFile = AttachmentMultiPartFile.create(attachment, content);
		final var file = File.createTempFile("test_", null);

		// Act
		multipartFile.transferTo(file);

		// Assert
		assertThat(file).exists();
		assertThat(FileUtils.readFileToByteArray(file)).isEqualTo(content);
	}

	@Test
	void transferToForAttachmentWithoutContent() {

		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		// Act & Assert
		assertThrows(NullPointerException.class, () -> AttachmentMultiPartFile.create(attachment, null), "Content must be provided");
		verifyNoInteractions(fileMock);
	}
}
