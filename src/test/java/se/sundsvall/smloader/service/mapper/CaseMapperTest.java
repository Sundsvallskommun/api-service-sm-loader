package se.sundsvall.smloader.service.mapper;

import org.junit.jupiter.api.Test;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.DeliveryStatus;

import java.time.OffsetDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.db.model.DeliveryStatus.PENDING;

class CaseMapperTest {

	@Test
	void toCaseEntity() {
		final var xml = readOpenEFile("flow-instance-lamna-synpunkt.xml");

		final var caseEntity = CaseMapper.toCaseEntity("456", xml);

		assertThat(caseEntity.getId()).isEqualTo("456");
		assertThat(caseEntity.getFamilyId()).isEqualTo("161");
		assertThat(caseEntity.getOpenECase()).isEqualTo(new String(xml));
		assertThat(caseEntity.getDeliveryStatus()).isEqualTo(PENDING);
	}

	@Test
	void toCaseMapping() {
		final var errandId = "errandId";
		final var caseEntity = CaseEntity.create()
			.withId("caseId")
			.withFamilyId("familyId")
			.withOpenECase("openECase")
			.withDeliveryStatus(DeliveryStatus.PENDING);

		final var caseMapping = CaseMapper.toCaseMapping(errandId, caseEntity);

		assertThat(caseMapping.getErrandId()).isEqualTo(errandId);
		assertThat(caseMapping.getExternalCaseId()).isEqualTo("caseId");
		assertThat(caseMapping.getTimestamp()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
	}
}
