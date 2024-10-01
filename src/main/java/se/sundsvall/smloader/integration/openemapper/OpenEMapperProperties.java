package se.sundsvall.smloader.integration.openemapper;

public class OpenEMapperProperties {
	private String familyId;
	private String category;
	private String type;
	private String priority;
	private String label;

	public String getFamilyId() {
		return familyId;
	}

	public void setFamilyId(String familyId) {
		this.familyId = familyId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getLabel() { return label; }

	public void setLabel(String label) { this.label = label; }
}
