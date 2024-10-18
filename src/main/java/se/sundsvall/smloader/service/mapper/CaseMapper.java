package se.sundsvall.smloader.service.mapper;

import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMappingEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.Base64;

public final class CaseMapper {

	private CaseMapper() {}

	public static CaseEntity toCaseEntity(final String openECaseId, final CaseMetaDataEntity caseMetaDataEntity, final byte[] xml) {
		final var xmlContent = Base64.getEncoder().encodeToString(xml);

		return CaseEntity.create()
			.withCaseMetaData(caseMetaDataEntity)
			.withExternalCaseId(openECaseId)
			.withOpenECase(xmlContent)
			.withDeliveryStatus(DeliveryStatus.PENDING);
	}

	public static CaseMappingEntity toCaseMapping(final String errandId, final CaseEntity caseEntity) {
		return CaseMappingEntity.create()
			.withErrandId(errandId)
			.withCaseType(caseEntity.getCaseMetaData().getFamilyId())
			.withExternalCaseId(caseEntity.getExternalCaseId())
			.withMunicipalityId(caseEntity.getCaseMetaData().getMunicipalityId())
			.withModified(OffsetDateTime.now());
	}
}
