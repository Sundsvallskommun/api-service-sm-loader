package se.sundsvall.smloader.integration.db;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseId;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

@Transactional(isolation = READ_COMMITTED)
@CircuitBreaker(name = "CaseRepository")
public interface CaseRepository extends JpaRepository<CaseEntity, String> {
	List<CaseEntity> findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(String municipalityId, DeliveryStatus... deliveryStatus);

	List<CaseId> findIdsByCreatedBeforeAndCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(OffsetDateTime created, String municipalityId, DeliveryStatus... deliveryStatuses);

	boolean existsByExternalCaseIdAndCaseMetaDataEntityInstanceAndCaseMetaDataEntityMunicipalityId(String externalCaseId, Instance instance, String municipalityId);

	long countByCreatedBeforeAndCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(OffsetDateTime created, String municipalityId, DeliveryStatus... deliveryStatuses);
}
