package se.sundsvall.smloader.integration.db;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.smloader.TestUtil;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

/**
 * CaseRepository tests
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class CaseRepositoryTest {

	@Autowired
	private CaseRepository caseRepository;

	@Autowired
	private CaseMetaDataRepository caseMetaDataRepository;

	@Test
	void create() throws Exception {

		final var openECase = TestUtil.readOpenEFile("flow-instance-lamna-synpunkt.xml");
		final var externalCaseId = "externalCaseId";

		final var entity = new CaseEntity().withOpenECase(new String(openECase, ISO_8859_1)).withExternalCaseId(externalCaseId).withDeliveryStatus(DeliveryStatus.PENDING);

		// Call
		final var caseMetaDataEntity = caseMetaDataRepository.findById("161").get();
		entity.setCaseMetaData(caseMetaDataEntity);
		final var result = caseRepository.save(entity);

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getId()).isNotNull();
		assertThat(result.getCaseMetaData()).isEqualTo(caseMetaDataEntity);
		assertThat(result.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(result.getOpenECase()).isEqualTo(new String(openECase, ISO_8859_1));
		assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
	}
}
