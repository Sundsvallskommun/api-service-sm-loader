package se.sundsvall.smloader.service.mapper;

import org.junit.jupiter.api.Test;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

import java.time.OffsetDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;

class CaseMapperTest {

	@Test
	void toCaseEntity() throws Exception {
		final var xml = readOpenEFile("flow-instance-lamna-synpunkt.xml");



		final var caseEntity = CaseMapper.toCaseEntity("456", EXTERNAL, xml);

		assertThat(caseEntity.getFamilyId()).isEqualTo("161");
		assertThat(caseEntity.getExternalCaseId()).isEqualTo("456");
		assertThat(caseEntity.getInstance()).isEqualTo(EXTERNAL);
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
		assertThat(caseMapping.getModified()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
	}
}
