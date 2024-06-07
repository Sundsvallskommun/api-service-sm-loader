package se.sundsvall.smloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.Length;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;

@Entity
@Table(name = "'case'",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_open-e_case_id_instance", columnNames = {"open-e-case-id", "instance"})
	})
public class CaseEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "family_id")
	private String familyId;

	@Column(name = "open-e-case-id")
	private String openECaseId;

	@Column(name = "instance")
	@Enumerated(EnumType.STRING)
	private Instance instance;


	@Column(name = "open-e-case", length = Length.LONG32)
	private String openECase;

	@Column(name = "delivery_status")
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

	public String getOpenECaseId() {
		return openECaseId;
	}

	public void setOpenECaseId(String openECaseId) {
		this.openECaseId = openECaseId;
	}

	public CaseEntity withOpenECaseId(String openECaseId) {
		this.openECaseId = openECaseId;
		return this;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public CaseEntity withInstance(Instance instance) {
		this.instance = instance;
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
		return Objects.hash(getId(), getFamilyId(), getOpenECaseId(), getInstance(), getOpenECase(), getDeliveryStatus());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CaseEntity that = (CaseEntity) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getFamilyId(), that.getFamilyId()) && Objects.equals(getOpenECaseId(), that.getOpenECaseId()) && Objects.equals(getInstance(), that.getInstance()) && Objects.equals(getOpenECase(), that.getOpenECase()) && getDeliveryStatus() == that.getDeliveryStatus();
	}

	@Override
	public String toString() {
		return new StringBuilder("CaseEntity [id=").append(id).append(", familyId=").append(familyId).append(", openECaseId=").append(openECaseId)
			.append(", instance=").append(instance).append(", openECase=").append(openECase).append(", deliveryStatus=")
			.append(deliveryStatus).append("]").toString();
	}
}
