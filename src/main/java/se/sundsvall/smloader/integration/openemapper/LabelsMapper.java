package se.sundsvall.smloader.integration.openemapper;

import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.Label;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class LabelsMapper {

	private LabelsMapper() {}

	public static List<ErrandLabel> mapLabels(final List<Label> labels, final List<String> labelResources) {
		if (labels == null || labels.isEmpty()) {
			return Collections.emptyList();
		}

		Stream<ErrandLabel> currentLevelStream = labels.stream()
			.filter(label -> labelResources.contains(label.getResourcePath()))
			.map(label -> new ErrandLabel().id(label.getId()));

		Stream<ErrandLabel> subLabelsStream = labels.stream()
			.map(Label::getLabels)
			.map(subLabels -> mapLabels(subLabels, labelResources))
			.flatMap(List::stream);

		return Stream.concat(currentLevelStream, subLabelsStream)
			.toList();
	}
}
