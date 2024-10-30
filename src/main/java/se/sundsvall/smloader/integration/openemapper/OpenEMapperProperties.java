package se.sundsvall.smloader.integration.openemapper;

import java.util.List;

public class OpenEMapperProperties {
	private String familyId;
	private String category;
	private String type;
	private String priority;
	private List<String> label;

	public String getFamilyId() {
		return familyId;
	}

	public void setFamilyId(final String familyId) {
		this.familyId = familyId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(final String priority) {
		this.priority = priority;
	}

	public List<String> getLabels() {
		return label;
	}

	public void setLabels(final List<String> label) {
		this.label = label;
	}
}
