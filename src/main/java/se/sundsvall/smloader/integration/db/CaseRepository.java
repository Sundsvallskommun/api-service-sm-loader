package se.sundsvall.smloader.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.DeliveryStatus;

import java.util.List;

public interface CaseRepository extends JpaRepository<CaseEntity, String> {
	List<CaseEntity> findAllByDeliveryStatus(DeliveryStatus deliveryStatus);
}
