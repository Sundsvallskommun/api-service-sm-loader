package se.sundsvall.smloader.integration.util;

import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Labels;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;

@ExtendWith(MockitoExtension.class)
class LabelsProviderTest {

	@Mock
	private SupportManagementClient supportManagementClientMock;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepositoryMock;

	@InjectMocks
	private LabelsProvider labelsProvider;

	@Test
	void refresh() {
		// Arrange
		final var municipalityId = "2281";
		final var classification = "classification";
		final var resourceName = "resourceName";
		final var resourcePath = "resourcePath";
		final var displayName = "displayName";
		final var namespace = "namespace";
		Label label = new Label().classification(classification).resourceName(resourceName).resourcePath(resourcePath).displayName(displayName);
		Labels labels = new Labels().addLabelStructureItem(label);
		final var caseMetaDataEntity = CaseMetaDataEntity.create().withInstance(EXTERNAL).withMunicipalityId(municipalityId).withNamespace(namespace);

		when(caseMetaDataRepositoryMock.findAll()).thenReturn(List.of(caseMetaDataEntity));
		when(supportManagementClientMock.getLabels(municipalityId, namespace)).thenReturn(ResponseEntity.of(Optional.of(labels)));

		// Act
		labelsProvider.refresh();

		// Assert
		verify(caseMetaDataRepositoryMock).findAll();
		verify(supportManagementClientMock).getLabels(municipalityId, namespace);
		verifyNoMoreInteractions(caseMetaDataRepositoryMock, supportManagementClientMock);
	}

	@Test
	void refreshWhenNoCaseMetaData() {
		// Arrange
		when(caseMetaDataRepositoryMock.findAll()).thenReturn(List.of());

		// Act
		labelsProvider.refresh();

		// Assert
		verify(caseMetaDataRepositoryMock).findAll();
		verifyNoMoreInteractions(caseMetaDataRepositoryMock, supportManagementClientMock);
	}

	@Test
	void refreshWhenException() {
		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var caseMetaDataEntity = CaseMetaDataEntity.create().withInstance(EXTERNAL).withMunicipalityId(municipalityId).withNamespace(namespace);

		when(caseMetaDataRepositoryMock.findAll()).thenReturn(List.of(caseMetaDataEntity));
		when(supportManagementClientMock.getLabels(municipalityId, namespace)).thenThrow(new RuntimeException("Something went wrong"));

		// Act
		labelsProvider.refresh();

		// Assert
		verify(caseMetaDataRepositoryMock).findAll();
		verify(supportManagementClientMock).getLabels(municipalityId, namespace);
		verifyNoMoreInteractions(caseMetaDataRepositoryMock, supportManagementClientMock);
	}

	@Test
	void getLabels() {
		// Arrange
		final var municipalityId = "2281";
		final var classification = "classification";
		final var resourceName = "resourceName";
		final var resourcePath = "resourcePath";
		final var displayName = "displayName";
		final var namespace = "namespace";
		Label label = new Label().classification(classification).resourceName(resourceName).resourcePath(resourcePath).displayName(displayName);
		Labels labels = new Labels().addLabelStructureItem(label);
		final var caseMetaDataEntity = CaseMetaDataEntity.create().withInstance(EXTERNAL).withMunicipalityId(municipalityId).withNamespace(namespace);

		when(caseMetaDataRepositoryMock.findAll()).thenReturn(List.of(caseMetaDataEntity));
		when(supportManagementClientMock.getLabels(municipalityId, namespace)).thenReturn(ResponseEntity.of(Optional.of(labels)));
		// Load labels
		labelsProvider.refresh();

		// Act
		final var result = labelsProvider.getLabels(namespace);

		// Assert
		verify(caseMetaDataRepositoryMock).findAll();
		verify(supportManagementClientMock).getLabels(municipalityId, namespace);

		assertThat(result).hasSize(1).containsExactly(label);
	}
}
