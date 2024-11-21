package se.sundsvall.smloader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseMappingEntity;
import se.sundsvall.smloader.integration.db.model.CaseMappingId;

@Transactional
@CircuitBreaker(name = "CaseMappingRepository")
public interface CaseMappingRepository extends JpaRepository<CaseMappingEntity, CaseMappingId> {
	void deleteByModifiedBeforeAndMunicipalityId(OffsetDateTime modified, String municipalityId);
}
