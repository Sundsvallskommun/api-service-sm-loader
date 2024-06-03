package se.sundsvall.smloader.integration.db.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@IdClass(CaseMappingId.class)
@Table(name = "case_mapping",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_external_case_id", columnNames = {"externalCaseId"})
	})
public class CaseMapping {

	@Id
	@Column(unique = true, name = "externalCaseId")
	private String externalCaseId;

	@Id
	@Column(name = "errandId")
	private String errandId;

	@NotNull
	@Column(name = "caseType", columnDefinition = "varchar(255)")
	private String caseType;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(name = "timestamp")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime timestamp;

	public static CaseMapping create() {
		return new CaseMapping();
	}

	@PrePersist
	@PreUpdate
	protected void onPersistAndUpdate() {
		timestamp = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
	}

	public String getExternalCaseId() {
		return externalCaseId;
	}

	public void setExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
	}

	public CaseMapping withExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
		return this;
	}

	public String getErrandId() {
		return errandId;
	}

	public void setErrandId(String errandId) {
		this.errandId = errandId;
	}

	public CaseMapping withErrandId(String errandId) {
		this.errandId = errandId;
		return this;
	}

	public @NotNull String getCaseType() {
		return caseType;
	}

	public void setCaseType(@NotNull String caseType) {
		this.caseType = caseType;
	}

	public CaseMapping withCaseType(@NotNull String caseType) {
		this.caseType = caseType;
		return this;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public CaseMapping withTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getExternalCaseId(), getErrandId(), getCaseType(), getTimestamp());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CaseMapping that = (CaseMapping) o;
		return Objects.equals(getExternalCaseId(), that.getExternalCaseId()) && Objects.equals(getErrandId(), that.getErrandId()) && Objects.equals(getCaseType(), that.getCaseType()) && Objects.equals(getTimestamp(), that.getTimestamp());
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder.append("CaseMapping [externalCaseId=").append(externalCaseId).append(", errandId=" + errandId)
			.append(", caseType=").append(caseType).append(", timestamp=").append(timestamp).append("]").toString();
	}
}
