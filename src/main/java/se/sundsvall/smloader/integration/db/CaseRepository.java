package se.sundsvall.smloader.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.smloader.integration.db.model.CaseEntity;

public interface CaseRepository extends JpaRepository<CaseEntity, String> {
}
