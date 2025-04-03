package se.sundsvall.smloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

@Entity
@Table(name = "case_meta_data")
public class CaseMetaDataEntity {

	@Id
	@Column(name = "family_id")
	private String familyId;

	@Column(name = "instance")
	private Instance instance;

	@Column(name = "open_e_update_status")
	private String openEUpdateStatus;

	@Column(name = "open_e_import_status")
	private String openEImportStatus;

	@Column(name = "namespace")
	private String namespace;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "stats_only", columnDefinition = "bit default 0")
	private boolean statsOnly;

	public static CaseMetaDataEntity create() {
		return new CaseMetaDataEntity();
	}

	public String getFamilyId() {
		return familyId;
	}

	public void setFamilyId(String familyId) {
		this.familyId = familyId;
	}

	public CaseMetaDataEntity withFamilyId(String familyId) {
		this.familyId = familyId;
		return this;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public CaseMetaDataEntity withInstance(Instance instance) {
		this.instance = instance;
		return this;
	}

	public String getOpenEUpdateStatus() {
		return openEUpdateStatus;
	}

	public void setOpenEUpdateStatus(String openEUpdateStatus) {
		this.openEUpdateStatus = openEUpdateStatus;
	}

	public CaseMetaDataEntity withOpenEUpdateStatus(String openEUpdateStatus) {
		this.openEUpdateStatus = openEUpdateStatus;
		return this;
	}

	public String getOpenEImportStatus() {
		return openEImportStatus;
	}

	public void setOpenEImportStatus(String openEImportStatus) {
		this.openEImportStatus = openEImportStatus;
	}

	public CaseMetaDataEntity withOpenEImportStatus(String openEImportStatus) {
		this.openEImportStatus = openEImportStatus;
		return this;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public CaseMetaDataEntity withNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public CaseMetaDataEntity withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public boolean isStatsOnly() {
		return statsOnly;
	}

	public void setStatsOnly(boolean statsOnly) {
		this.statsOnly = statsOnly;
	}

	public CaseMetaDataEntity withStatsOnly(boolean statsOnly) {
		this.statsOnly = statsOnly;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CaseMetaDataEntity that = (CaseMetaDataEntity) o;
		return Objects.equals(familyId, that.familyId) && instance == that.instance && Objects.equals(openEUpdateStatus, that.openEUpdateStatus) && Objects.equals(openEImportStatus, that.openEImportStatus) &&
			Objects.equals(namespace, that.namespace) && Objects.equals(municipalityId, that.municipalityId) && statsOnly == that.statsOnly;
	}

	@Override
	public int hashCode() {
		return Objects.hash(familyId, instance, openEUpdateStatus, openEImportStatus, namespace, municipalityId, statsOnly);
	}

	@Override
	public String toString() {
		return new StringBuilder("CaseMetaDataEntity [familyId=").append(familyId).append(", instance=").append(instance).append(", openEUpdateStatus=").append(openEUpdateStatus).append(", openEImportStatus=").append(openEImportStatus)
			.append(", namespace=").append(namespace).append(", municipalityId=").append(municipalityId).append(", statsOnly=").append(statsOnly).append("]").toString();
	}

}
