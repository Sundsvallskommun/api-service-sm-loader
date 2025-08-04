package se.sundsvall.smloader.service.mapper;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;
import static se.sundsvall.smloader.integration.db.model.enums.Instance.EXTERNAL;

import java.time.OffsetDateTime;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

@ExtendWith(ResourceLoaderExtension.class)
class CaseMapperTest {

	@Test
	void toCaseEntity(@Load("open-e/flow-instance-lamna-synpunkt.xml") final String xml) {
		final var caseMetaDataEntity = CaseMetaDataEntity.create()
			.withFamilyId("161")
			.withInstance(EXTERNAL)
			.withNamespace("namespace")
			.withMunicipalityId("municipalityId")
			.withOpenEImportStatus("openEImportStatus");

		final var caseEntity = CaseMapper.toCaseEntity("456", caseMetaDataEntity, xml);

		assertThat(caseEntity.getCaseMetaData()).isEqualTo(caseMetaDataEntity);
		assertThat(caseEntity.getExternalCaseId()).isEqualTo("456");
		assertThat(caseEntity.getOpenECase()).isEqualTo(Base64.getEncoder().encodeToString(xml.getBytes()));
		assertThat(caseEntity.getDeliveryStatus()).isEqualTo(PENDING);
	}

	@Test
	void toCaseMapping() {
		final var errandId = "errandId";
		final var externalCaseId = "externalCaseId";
		final var familyId = "familyId";
		final var municipalityId = "municipalityId";
		final var caseEntity = CaseEntity.create()
			.withExternalCaseId(externalCaseId)
			.withOpenECase("openECase")
			.withDeliveryStatus(DeliveryStatus.PENDING)
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withFamilyId(familyId)
				.withMunicipalityId(municipalityId));

		final var caseMapping = CaseMapper.toCaseMapping(errandId, caseEntity);

		assertThat(caseMapping.getErrandId()).isEqualTo(errandId);
		assertThat(caseMapping.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(caseMapping.getCaseType()).isEqualTo(familyId);
		assertThat(caseMapping.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(caseMapping.getModified()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
	}
}
