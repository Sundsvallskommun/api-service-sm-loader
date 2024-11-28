package se.sundsvall.smloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

@Entity
@IdClass(CaseMappingId.class)
@Table(name = "case_mapping",
	indexes = {
		@Index(name = "municipality_id_index", columnList = "municipality_id")
	})
public class CaseMappingEntity {

	@Id
	@Column(name = "external_case_id")
	private String externalCaseId;

	@Id
	@Column(name = "errand_id")
	private String errandId;

	@Column(name = "case_type")
	private String caseType;

	@Column(name = "modified")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime modified;

	@Column(name = "municipality_id")
	private String municipalityId;

	public static CaseMappingEntity create() {
		return new CaseMappingEntity();
	}

	@PrePersist
	@PreUpdate
	protected void onPersistAndUpdate() {
		modified = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
	}

	public String getExternalCaseId() {
		return externalCaseId;
	}

	public void setExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
	}

	public CaseMappingEntity withExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
		return this;
	}

	public String getErrandId() {
		return errandId;
	}

	public void setErrandId(String errandId) {
		this.errandId = errandId;
	}

	public CaseMappingEntity withErrandId(String errandId) {
		this.errandId = errandId;
		return this;
	}

	public @NotNull String getCaseType() {
		return caseType;
	}

	public void setCaseType(@NotNull String caseType) {
		this.caseType = caseType;
	}

	public CaseMappingEntity withCaseType(@NotNull String caseType) {
		this.caseType = caseType;
		return this;
	}

	public OffsetDateTime getModified() {
		return modified;
	}

	public void setModified(OffsetDateTime modified) {
		this.modified = modified;
	}

	public CaseMappingEntity withModified(OffsetDateTime modified) {
		this.modified = modified;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public CaseMappingEntity withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getExternalCaseId(), getErrandId(), getCaseType(), getModified(), getMunicipalityId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CaseMappingEntity that = (CaseMappingEntity) o;
		return Objects.equals(getExternalCaseId(), that.getExternalCaseId()) && Objects.equals(getErrandId(), that.getErrandId()) && Objects.equals(getCaseType(), that.getCaseType()) && Objects.equals(getModified(), that.getModified())
			&& Objects.equals(getMunicipalityId(), that.getMunicipalityId());
	}

	@Override
	public String toString() {
		return new StringBuilder().append("CaseMappingEntity [externalCaseId=").append(externalCaseId).append(", errandId=" + errandId)
			.append(", caseType=").append(caseType).append(", modified=").append(modified).append(", municipalityId=").append(municipalityId).append("]").toString();
	}
}
