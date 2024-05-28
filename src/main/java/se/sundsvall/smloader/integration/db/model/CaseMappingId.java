package se.sundsvall.smloader.integration.db.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class CaseMappingId implements Serializable {

	@Serial
	private static final long serialVersionUID = -6931529624351524472L;

	private String externalCaseId;

	private String caseId;

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

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public CaseMappingId withCaseId(String caseId) {
		this.caseId = caseId;
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
		return Objects.equals(externalCaseId, caseMappingId.externalCaseId) && Objects.equals(caseId, caseMappingId.caseId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(externalCaseId, caseId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder.append("CaseMappingId [externalCaseId=").append(externalCaseId).append(", caseId=").append(caseId).append("]").toString();
	}
}
