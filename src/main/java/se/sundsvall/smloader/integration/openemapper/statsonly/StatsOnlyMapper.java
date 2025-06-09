package se.sundsvall.smloader.integration.openemapper.statsonly;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Priority;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;
import se.sundsvall.smloader.integration.openemapper.OpenEStatsOnlyMapperProperties;

@Component
public class StatsOnlyMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatsOnlyMapper.class);
	private static final String UNKNOWN = "unknown";

	private final OpenEStatsOnlyMapperProperties properties;

	public StatsOnlyMapper(final OpenEStatsOnlyMapperProperties properties) {
		this.properties = properties;
	}

	public Optional<Errand> mapToErrand(final CaseEntity caseEntity, final String familyId, final Instance instance) {
		try {
			if (isNull(properties) || isNull(properties.getServices().get(familyId))) {
				LOGGER.error("No stats-only config found for familyId: {}", familyId);
				return Optional.empty();
			}

			final var serviceProperties = properties.getServices().get(familyId);

			return Optional.of(new Errand()
				.title(serviceProperties.getServiceName())
				.status(STATUS_NEW)
				.priority(getPriority(serviceProperties.getPriority()))
				.classification(new Classification().category(serviceProperties.getCategory()).type(serviceProperties.getType()))
				.channel(getChannel(instance))
				.businessRelated(false)
				.labels(Optional.ofNullable(serviceProperties.getLabels()).orElse(emptyList()))
				.reporterUserId(UNKNOWN)
				.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(caseEntity.getExternalCaseId()))));
		} catch (Exception e) {
			LOGGER.error("Error mapping to Errand with familyId {}: {}", familyId, e.getMessage(), e);
			return Optional.empty();
		}
	}

	private Priority getPriority(final String priority) {
		return priority != null ? Priority.fromValue(priority) : null;
	}

	private String getChannel(Instance instance) {
		return instance == Instance.EXTERNAL ? EXTERNAL_CHANNEL_E_SERVICE : INTERNAL_CHANNEL_E_SERVICE;
	}
}
