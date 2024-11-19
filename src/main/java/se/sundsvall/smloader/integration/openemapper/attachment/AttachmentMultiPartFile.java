package se.sundsvall.smloader.integration.openemapper.attachment;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class AttachmentMultiPartFile implements MultipartFile {

	private final Attachment attachment;
	private final InputStream contentStream;

	private AttachmentMultiPartFile(final Attachment attachment, final InputStream contentStream) {
		this.attachment = attachment;
		this.contentStream = contentStream;
	}

	public static AttachmentMultiPartFile create(final Attachment attachment, final InputStream contentStream) {
		requireNonNull(attachment, "Attachment must be provided");
		requireNonNull(contentStream, "Content stream must be provided");
		return new AttachmentMultiPartFile(attachment, contentStream);
	}

	@NotNull
	@Override
	public String getName() {
		return attachment.getFileName();
	}

	@Override
	public String getOriginalFilename() {
		return attachment.getFileName();
	}

	@Override
	public String getContentType() {
		return "";
	}

	@Override
	public boolean isEmpty() {
		try {
			return contentStream.available() == 0;
		} catch (final IOException e) {
			return true;
		}
	}

	@Override
	public long getSize() {

		try {
			return contentStream.available();
		} catch (final IOException e) {
			return 0;
		}

	}

	@NotNull
	@Override
	public byte[] getBytes() throws IOException {
		return contentStream.readAllBytes();
	}

	@NotNull
	@Override
	public InputStream getInputStream() {
		return contentStream;
	}

	@Override
	public void transferTo(@NotNull final File dest) throws IOException, IllegalStateException {
		try (final FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
			contentStream.transferTo(fileOutputStream);
		}
	}
}
