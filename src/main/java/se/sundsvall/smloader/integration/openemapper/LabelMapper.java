package se.sundsvall.smloader.integration.openemapper;

import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.Label;

public final class LabelMapper {

	private LabelMapper() {}

	public static ErrandLabel toErrandLabel(final Label label) {

		return new ErrandLabel()
			.classification(label.getClassification())
			.displayName(label.getDisplayName())
			.resourceName(label.getResourceName())
			.resourcePath(label.getResourcePath());
	}
}
