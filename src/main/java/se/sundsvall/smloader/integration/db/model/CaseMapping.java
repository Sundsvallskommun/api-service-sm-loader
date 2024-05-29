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
	@Column(name = "caseId")
	private String caseId;

	@NotNull
	@Column(name = "caseType", columnDefinition = "varchar(255)")
	private String caseType;

	@Column(name = "serviceName")
	private String serviceName;

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

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public CaseMapping withCaseId(String caseId) {
		this.caseId = caseId;
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

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public CaseMapping withServiceName(String serviceName) {
		this.serviceName = serviceName;
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
		return Objects.hash(getExternalCaseId(), getCaseId(), getCaseType(), getServiceName(), getTimestamp());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CaseMapping that = (CaseMapping) o;
		return Objects.equals(getExternalCaseId(), that.getExternalCaseId()) && Objects.equals(getCaseId(), that.getCaseId()) && Objects.equals(getCaseType(), that.getCaseType()) && Objects.equals(getServiceName(), that.getServiceName()) && Objects.equals(getTimestamp(), that.getTimestamp());
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder.append("CaseMapping [externalCaseId=").append(externalCaseId).append(", caseId=" + caseId)
			.append(", caseType=").append(caseType).append(", serviceName=").append(serviceName).append(", timestamp=").append(timestamp).append("]").toString();
	}
}
