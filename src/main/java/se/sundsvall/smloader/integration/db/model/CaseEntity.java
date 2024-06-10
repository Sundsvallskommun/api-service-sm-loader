package se.sundsvall.smloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.Length;
import org.hibernate.annotations.UuidGenerator;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

import java.util.Objects;

@Entity
@Table(name = "'case'",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_external_case_id_instance", columnNames = {"external_case_id", "instance"})
	})
public class CaseEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "family_id")
	private String familyId;

	@Column(name = "external_case_id")
	private String externalCaseId;

	@Column(name = "instance")
	private Instance instance;


	@Column(name = "open_e_case", length = Length.LONG32)
	private String openECase;

	@Column(name = "delivery_status")
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
		return Objects.hash(getId(), getFamilyId(), getExternalCaseId(), getInstance(), getOpenECase(), getDeliveryStatus());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CaseEntity that = (CaseEntity) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getFamilyId(), that.getFamilyId()) && Objects.equals(getExternalCaseId(), that.getExternalCaseId()) && Objects.equals(getInstance(), that.getInstance()) && Objects.equals(getOpenECase(), that.getOpenECase()) && getDeliveryStatus() == that.getDeliveryStatus();
	}

	@Override
	public String toString() {
		return new StringBuilder("CaseEntity [id=").append(id).append(", familyId=").append(familyId).append(", externalCaseId=").append(externalCaseId)
			.append(", instance=").append(instance).append(", openECase=").append(openECase).append(", deliveryStatus=")
			.append(deliveryStatus).append("]").toString();
	}
}
