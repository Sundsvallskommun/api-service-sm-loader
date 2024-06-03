package se.sundsvall.smloader.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.smloader.integration.db.model.CaseMapping;
import se.sundsvall.smloader.integration.db.model.CaseMappingId;

import java.util.List;

public interface CaseMappingRepository extends JpaRepository<CaseMapping, CaseMappingId> {

	List<CaseMapping> findAllByExternalCaseIdOrErrandId(String externalCaseId, String errandId);

	List<CaseMapping> findAllByExternalCaseId(String externalCaseId);

	boolean existsByExternalCaseId(String externalCaseId);
}
