package se.sundsvall.smloader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

@Transactional
@CircuitBreaker(name = "CaseMetaDataRepository")
public interface CaseMetaDataRepository extends JpaRepository<CaseMetaDataEntity, String> {

	List<CaseMetaDataEntity> findByInstanceAndMunicipalityIdAndStatsOnly(final Instance instance, final String municipalityId, final boolean statsOnly);

	List<CaseMetaDataEntity> findByMunicipalityIdAndStatsOnly(final String municipalityId, final boolean statsOnly);

	CaseMetaDataEntity findByFamilyId(final String familyId);
}
