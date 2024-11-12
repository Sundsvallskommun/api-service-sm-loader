package se.sundsvall.smloader.integration.openemapper.attachment;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.io.InputStream.nullInputStream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class AttachmentMultiPartFile implements MultipartFile {

	private final Attachment attachment;
	private final byte[] content;

	private AttachmentMultiPartFile(final Attachment attachment, final byte[] content) {
		this.attachment = attachment;
		this.content = content;
	}

	public static AttachmentMultiPartFile create(final Attachment attachment, final byte[] content) {
		requireNonNull(attachment, "Attachment must be provided");
		requireNonNull(content, "Content must be provided");
		return new AttachmentMultiPartFile(attachment, content);
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
		return isNull(content) || content.length == 0;
	}

	@Override
	public long getSize() {
		return isNull(content) ? 0 : content.length;
	}

	@NotNull
	@Override
	public byte[] getBytes() throws IOException {
		return content;
	}

	@NotNull
	@Override
	public InputStream getInputStream() throws IOException {
		return isNull(content) ? nullInputStream() : new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(@NotNull final File dest) throws IOException, IllegalStateException {
		if (nonNull(content)) {
			try (final FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
				fileOutputStream.write(content);
			}
		}
	}
}
