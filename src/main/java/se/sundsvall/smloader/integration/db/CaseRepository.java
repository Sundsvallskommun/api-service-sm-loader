package se.sundsvall.smloader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseId;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Transactional(isolation = READ_COMMITTED)
@CircuitBreaker(name = "CaseRepository")
public interface CaseRepository extends JpaRepository<CaseEntity, String> {
	List<CaseEntity> findAllByDeliveryStatus(DeliveryStatus deliveryStatus);

	List<CaseId> findIdsByCreatedBeforeAndDeliveryStatusIn(OffsetDateTime created, DeliveryStatus... deliveryStatuses);

	boolean existsByExternalCaseIdAndCaseMetaDataEntityInstance(String externalCaseId, Instance instance);

	long countByCreatedBeforeAndDeliveryStatusIn(OffsetDateTime created, DeliveryStatus... deliveryStatuses);
}
