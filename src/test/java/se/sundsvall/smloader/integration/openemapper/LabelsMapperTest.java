package se.sundsvall.smloader.integration.openemapper;

import static org.assertj.core.api.Assertions.assertThat;

import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.Label;
import java.util.List;
import org.junit.jupiter.api.Test;

class LabelsMapperTest {

	@Test
	void mapLabels() {
		// Arrange
		final var labelId_1 = "labelId_1";
		final var labelId_2 = "labelId_2";
		final var labelId_3 = "labelId_3";
		final var label_1 = "label_1";
		final var label_2 = "label_2";
		final var label_3 = "label_3";
		final var resourceName = "resourceName";
		final var classification = "classification";
		final var displayName = "displayName";
		final var labels = List.of(label_1, label_2, label_3);

		final var metaLabels = List.of(
			new Label().id(labelId_1).resourcePath(label_1).resourceName(resourceName).classification(classification).displayName(displayName)
				.labels(List.of(new Label().id(labelId_2).resourcePath(label_2).resourceName(resourceName).classification(classification).displayName(displayName)
					.labels(List.of(new Label().id(labelId_3).resourcePath(label_3).resourceName(resourceName).classification(classification).displayName(displayName))))));

		// Act
		final var mappedLabels = LabelsMapper.mapLabels(metaLabels, labels);

		// Assert
		assertThat(mappedLabels).extracting(ErrandLabel::getId).containsExactly(labelId_1, labelId_2, labelId_3);
	}
}
