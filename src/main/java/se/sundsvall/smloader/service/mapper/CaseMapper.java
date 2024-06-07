package se.sundsvall.smloader.service.mapper;

import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMappingEntity;
import se.sundsvall.smloader.integration.db.model.DeliveryStatus;
import se.sundsvall.smloader.integration.db.model.Instance;

import java.time.OffsetDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

public final class CaseMapper {

	private CaseMapper() {}

	public static CaseEntity toCaseEntity(final String openECaseId, final Instance instance, final byte[] xml) {
		final var xmlContent = new String(xml, UTF_8);
		final var result = evaluateXPath(xml, "/FlowInstance/Header/Flow/FamilyID");
		final var familyId = result.eachText().stream().map(String::trim).findFirst().orElse(null);

		return CaseEntity.create()
			.withFamilyId(familyId)
			.withOpenECaseId(openECaseId)
			.withInstance(instance)
			.withOpenECase(xmlContent)
			.withDeliveryStatus(DeliveryStatus.PENDING);
	}

	public static CaseMappingEntity toCaseMapping(final String errandId, final CaseEntity caseEntity) {
		return CaseMappingEntity.create()
			.withErrandId(errandId)
			.withExternalCaseId(caseEntity.getId())
			.withModified(OffsetDateTime.now());
	}
}
