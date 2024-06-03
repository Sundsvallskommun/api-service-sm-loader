package se.sundsvall.smloader.service.mapper;

import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMapping;
import se.sundsvall.smloader.integration.db.model.DeliveryStatus;

import java.time.OffsetDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

public class CaseMapper {

	private CaseMapper() {}

	public static CaseEntity toCaseEntity(final String id, final byte[] xml) {
		final var xmlContent = new String(xml, UTF_8);
		final var result = evaluateXPath(xml, "/FlowInstance/Header/Flow/FamilyID");
		final var familyId = result.eachText().stream().map(String::trim).findFirst().orElse(null);

		return CaseEntity.create()
				.withId(id)
				.withFamilyId(familyId)
				.withOpenECase(xmlContent)
				.withDeliveryStatus(DeliveryStatus.PENDING);
	}

	public static CaseMapping toCaseMapping(final String errandId, final CaseEntity caseEntity) {
		return CaseMapping.create()
			.withErrandId(errandId)
			.withExternalCaseId(caseEntity.getId())
			.withTimestamp(OffsetDateTime.now());
	}
}
