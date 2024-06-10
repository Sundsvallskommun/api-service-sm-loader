package se.sundsvall.smloader.integration.db.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class CaseMappingId implements Serializable {

	@Serial
	private static final long serialVersionUID = -6931529624351524472L;

	private String externalCaseId;

	private String errandId;

	public static CaseMappingId create() {
		return new CaseMappingId();
	}

	public String getExternalCaseId() {
		return externalCaseId;
	}

	public void setExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
	}

	public CaseMappingId withExternalCaseId(String externalCaseId) {
		this.externalCaseId = externalCaseId;
		return this;
	}

	public String getErrandId() {
		return errandId;
	}

	public void setErrandId(String errandId) {
		this.errandId = errandId;
	}

	public CaseMappingId withErrandId(String errandId) {
		this.errandId = errandId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}
		final CaseMappingId caseMappingId = (CaseMappingId) o;
		return Objects.equals(externalCaseId, caseMappingId.externalCaseId) && Objects.equals(errandId, caseMappingId.errandId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(externalCaseId, errandId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder.append("CaseMappingId [externalCaseId=").append(externalCaseId).append(", errandId=").append(errandId).append("]").toString();
	}
}
