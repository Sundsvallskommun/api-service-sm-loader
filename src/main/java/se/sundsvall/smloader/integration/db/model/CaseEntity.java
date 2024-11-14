package se.sundsvall.smloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.hibernate.Length;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UuidGenerator;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

@Entity
@Table(name = "'case'")
public class CaseEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@ManyToOne
	@JoinColumn(name = "family_id", nullable = false, foreignKey = @ForeignKey(name = "fk_case_case_meta_data_family_id"))
	private CaseMetaDataEntity caseMetaDataEntity;

	@Column(name = "external_case_id")
	private String externalCaseId;

	@Column(name = "open_e_case", length = Length.LONG32)
	private String openECase;

	@Column(name = "delivery_status")
	private DeliveryStatus deliveryStatus;

	@Column(name = "created")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	public static CaseEntity create() {
		return new CaseEntity();
	}

	@PrePersist
	protected void onPersist() {
		created = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CaseEntity withId(String id) {
		this.id = id;
		return this;
	}

	public CaseMetaDataEntity getCaseMetaData() {
		return caseMetaDataEntity;
	}

	public void setCaseMetaData(CaseMetaDataEntity caseMetaDataEntity) {
		this.caseMetaDataEntity = caseMetaDataEntity;
	}

	public CaseEntity withCaseMetaData(CaseMetaDataEntity caseMetaDataEntity) {
		this.caseMetaDataEntity = caseMetaDataEntity;
		return this;
	}

	public String getExternalCaseId() {
		return externalCaseId;
	}

	public void setexternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
	}

	public CaseEntity withExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
		return this;
	}

	public DeliveryStatus getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public CaseEntity withDeliveryStatus(DeliveryStatus deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
		return this;
	}

	public String getOpenECase() {
		return openECase;
	}

	public void setOpenECase(String openECase) {
		this.openECase = openECase;
	}

	public CaseEntity withOpenECase(String openECase) {
		this.openECase = openECase;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public CaseEntity withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getCaseMetaData(), getExternalCaseId(), getOpenECase(), getDeliveryStatus(), getCreated());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}
		final CaseEntity that = (CaseEntity) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getCaseMetaData(), that.caseMetaDataEntity) && Objects.equals(getExternalCaseId(), that.getExternalCaseId()) && Objects.equals(getOpenECase(), that.getOpenECase())
			&& (getDeliveryStatus() == that.getDeliveryStatus())
			&& Objects.equals(getCreated(), that.getCreated());
	}

	@Override
	public String toString() {
		return new StringBuilder("CaseEntity [id=").append(id).append(", caseMetaData=").append(caseMetaDataEntity).append(", externalCaseId=").append(externalCaseId)
			.append(", openECase=").append(openECase).append(", deliveryStatus=").append(deliveryStatus).append(", created=").append(created).append("]").toString();
	}
}
