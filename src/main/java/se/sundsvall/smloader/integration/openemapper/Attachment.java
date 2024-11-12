package se.sundsvall.smloader.integration.openemapper;

import java.util.Objects;

public class Attachment {
	private String fileId;
	private String fileName;
	private String queryId;

	public static Attachment create() {
		return new Attachment();
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(final String fileId) {
		this.fileId = fileId;
	}

	public Attachment withFileId(final String fileId) {
		this.fileId = fileId;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public Attachment withFileName(final String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(final String queryId) {
		this.queryId = queryId;
	}

	public Attachment withQueryId(final String queryId) {
		this.queryId = queryId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Attachment that = (Attachment) o;
		return Objects.equals(fileId, that.fileId) && Objects.equals(fileName, that.fileName) && Objects.equals(queryId, that.queryId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileId, fileName, queryId);
	}

	@Override
	public String toString() {
		return "Attachment{" + "fileId='" + fileId + '\''
			+ ", fileName='" + fileName + '\''
			+ ", queryId='" + queryId + '\''
			+ '}';
	}
}
