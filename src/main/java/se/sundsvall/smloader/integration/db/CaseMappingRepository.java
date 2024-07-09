package se.sundsvall.smloader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseMappingEntity;
import se.sundsvall.smloader.integration.db.model.CaseMappingId;

import java.time.OffsetDateTime;

@Transactional
@CircuitBreaker(name = "CaseMappingRepository")
public interface CaseMappingRepository extends JpaRepository<CaseMappingEntity, CaseMappingId> {
	void deleteByModifiedBefore(OffsetDateTime modified);
}
