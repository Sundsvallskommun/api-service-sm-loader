package se.sundsvall.smloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.Length;

import java.util.Objects;

@Entity
@Table(name = "'case'")
public class CaseEntity {

	@Id
	private String id;

	@Column(columnDefinition = "varchar(255)")
	private String familyId;

	@Column(length = Length.LONG32)
	private String openECase;

	@Column(columnDefinition = "varchar(255)")
	@Enumerated(EnumType.STRING)
	private DeliveryStatus deliveryStatus;

	public static CaseEntity create() {
		return new CaseEntity();
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

	public String getFamilyId() {
		return familyId;
	}

	public void setFamilyId(String familyId) {
		this.familyId = familyId;
	}

	public CaseEntity withFamilyId(String familyId) {
		this.familyId = familyId;
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

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getFamilyId(), getOpenECase(), getDeliveryStatus());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CaseEntity that = (CaseEntity) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getFamilyId(), that.getFamilyId()) && Objects.equals(getOpenECase(), that.getOpenECase()) && getDeliveryStatus() == that.getDeliveryStatus();
	}

	@Override
	public String toString() {
		return new StringBuilder("CaseEntity [id=").append(id).append(", familyId=").append(familyId)
			.append(", openECase=").append(openECase).append(", deliveryStatus=").append(deliveryStatus).append("]").toString();
	}
}
