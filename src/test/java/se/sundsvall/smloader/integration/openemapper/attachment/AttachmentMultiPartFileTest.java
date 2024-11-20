package se.sundsvall.smloader.integration.openemapper.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiremock.org.apache.commons.io.FileUtils;

@ExtendWith(MockitoExtension.class)
class AttachmentMultiPartFileTest {

	@Mock
	private File fileMock;

	@Mock
	private InputStream inputStreamMock;

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

		final var stream = new ByteArrayInputStream(content);

		// Act
		final var multipartFile = AttachmentMultiPartFile.create(attachment, stream);

		// Assert
		assertThat(multipartFile.getBytes()).isEqualTo(content);
		assertThat(multipartFile.getContentType()).isEmpty();
		assertThat(multipartFile.getInputStream()).isEqualTo(stream);
		assertThat(multipartFile.getName()).isEqualTo(fileName);
		assertThat(multipartFile.getOriginalFilename()).isEqualTo(fileName);
		assertThat(multipartFile.getSize()).isZero();
		assertThat(multipartFile.isEmpty()).isTrue();
	}

	@Test
	void getSizeThrowsIOException() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		when(inputStreamMock.available()).thenThrow(new IOException("Test IOException"));

		final var multipartFile = AttachmentMultiPartFile.create(attachment, inputStreamMock);

		// Act & Assert
		assertThat(multipartFile.getSize()).isZero();
	}

	@Test
	void isEmptyThrowsIOException() throws Exception {
		// Arrange
		final var fileId = "fileId";
		final var fileName = "fileName";
		final var queryId = "queryId";
		final var attachment = Attachment.create()
			.withFileId(fileId)
			.withFileName(fileName)
			.withQueryId(queryId);

		when(inputStreamMock.available()).thenThrow(new IOException("Test IOException"));

		final var multipartFile = AttachmentMultiPartFile.create(attachment, inputStreamMock);

		// Act & Assert
		assertThat(multipartFile.isEmpty()).isTrue();
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
		final var stream = new ByteArrayInputStream(content.getBytes());

		// Act
		final var multipartFile = AttachmentMultiPartFile.create(attachment, stream);

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
		final var stream = new ByteArrayInputStream(content);
		final var multipartFile = AttachmentMultiPartFile.create(attachment, stream);
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
