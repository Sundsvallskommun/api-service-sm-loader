package se.sundsvall.smloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseId;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DatabaseCleanerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerService.class);

	private static final String LOG_ENTITIES_REMOVAL = "Removing a total of {} obsolete entities having status {}";
	private static final String LOG_NOTHING_TO_REMOVE = "No entities found with status {}, hence no obsolete entities to remove";

	private static final DeliveryStatus[] STATUS_FOR_ENTITIES_TO_REMOVE = {
		DeliveryStatus.CREATED, DeliveryStatus.FAILED
	};

	private final CaseRepository caseRepository;
	private final CaseMappingRepository caseMappingRepository;

	public DatabaseCleanerService(final CaseRepository caseRepository, final CaseMappingRepository caseMappingRepository) {
		this.caseRepository = caseRepository;
		this.caseMappingRepository = caseMappingRepository;
	}

	public void cleanDatabase(final OffsetDateTime deleteBefore, final String municipalityId) {
		final var entitiesToRemove = caseRepository.countByCreatedBeforeAndCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(deleteBefore, municipalityId, STATUS_FOR_ENTITIES_TO_REMOVE);
		if (entitiesToRemove > 0) {
			LOGGER.info(LOG_ENTITIES_REMOVAL, entitiesToRemove, STATUS_FOR_ENTITIES_TO_REMOVE);
			getIdsToRemove(deleteBefore, municipalityId).forEach(caseRepository::deleteById);

			caseMappingRepository.deleteByModifiedBeforeAndMunicipalityId(deleteBefore, municipalityId);
		} else {
			LOGGER.info(LOG_NOTHING_TO_REMOVE, (Object) STATUS_FOR_ENTITIES_TO_REMOVE);
		}
	}

	private List<String> getIdsToRemove(final OffsetDateTime deleteBeforeZoned, final String municipalityId) {
		return caseRepository.findIdsByCreatedBeforeAndCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(deleteBeforeZoned, municipalityId, STATUS_FOR_ENTITIES_TO_REMOVE)
			.stream()
			.map(CaseId::getId)
			.toList();
	}
}
