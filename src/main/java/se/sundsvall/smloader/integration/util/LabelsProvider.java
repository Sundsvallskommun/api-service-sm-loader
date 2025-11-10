package se.sundsvall.smloader.integration.util;

import static java.util.Collections.emptyList;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;

import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Labels;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

@Service
public class LabelsProvider implements ApplicationListener<ApplicationReadyEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelsProvider.class);
	private static final String LABELS_NOT_FOUND = "Labels not found for municipalityId: %s and namespace: %s";

	private final SupportManagementClient supportManagementClient;
	private final CaseMetaDataRepository caseMetaDataRepository;
	private final Map<String, List<Label>> cachedLabels = new LinkedHashMap<>();

	public LabelsProvider(final SupportManagementClient supportManagementClient, final CaseMetaDataRepository caseMetaDataRepository) {
		this.caseMetaDataRepository = caseMetaDataRepository;
		this.supportManagementClient = supportManagementClient;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		refresh();
	}

	public synchronized void refresh() {

		final var supportedNamespaces = caseMetaDataRepository.findAll()
			.stream()
			.filter(caseMetaDataEntity -> !caseMetaDataEntity.isStatsOnly())
			.map(CaseMetaDataEntity::getNamespace)
			.distinct()
			.filter(Objects::nonNull)
			.toList();

		supportedNamespaces.forEach(namespace -> {
			try {
				Labels labels = Optional.ofNullable(supportManagementClient.getLabels(MUNICIPALITY_ID, namespace))
					.map(ResponseEntity::getBody)
					.orElse(null);

				if (Objects.nonNull(labels) && labels.getLabelStructure() != null && !labels.getLabelStructure().isEmpty()) {
					cachedLabels
						.put(namespace, labels.getLabelStructure());
				} else {
					LOGGER.info(String.format(LABELS_NOT_FOUND, MUNICIPALITY_ID, namespace));
				}
			} catch (Exception e) {
				LOGGER.error("Failed fetch labels", e);
			}
		});
	}

	public Label getLabel(final String namespace, final String sourcePath) {
		return cachedLabels.getOrDefault(namespace, emptyList())
			.stream()
			.filter(label -> sourcePath.equals(label.getResourcePath()))
			.findFirst()
			.orElse(null);
	}

	public List<Label> getLabels(final String namespace) {
		return cachedLabels.getOrDefault(namespace, emptyList());
	}
}
