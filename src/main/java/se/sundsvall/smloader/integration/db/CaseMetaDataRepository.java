package se.sundsvall.smloader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

import java.util.List;

@Transactional
@CircuitBreaker(name = "CaseMetaDataRepository")
public interface CaseMetaDataRepository extends JpaRepository<CaseMetaDataEntity, String> {
	List<CaseMetaDataEntity> findByInstanceAndMunicipalityId(Instance instance, String municipalityId);
}
