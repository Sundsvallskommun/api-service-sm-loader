package se.sundsvall.smloader.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.smloader.Application;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.util.LabelsProvider;

/**
 * JobsIT tests.
 */
@WireMockAppTestSuite(files = "classpath:/JobsIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class JobsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/jobs";
	@Autowired
	private CaseRepository repository;

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseMetaDataRepository caseMetaDataRepository;

	@Autowired
	private LabelsProvider labelsProvider;

	@Test
	void test01_import() {

		// Assert that we don't have the records we are going to import.
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING).stream()
			.filter(caseEntity -> caseEntity.getExternalCaseId().equals("123456") || caseEntity.getExternalCaseId().equals("234567") ||
				caseEntity.getExternalCaseId().equals("111111"))
			.toList()).isEmpty();

		assertThat(caseMetaDataRepository.findAll()).hasSize(2);

		// Call
		setupCall()
			.withServicePath(PATH + "/caseimporter?from=2024-06-30T12:00:00&to=2024-07-02T12:00:00")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING).stream()
				.filter(caseEntity -> caseEntity.getExternalCaseId().equals("123456") || caseEntity.getExternalCaseId().equals("234567") ||
					caseEntity.getExternalCaseId().equals("111111"))
				.toList().size() == 3);
	}

	@Test
	@Disabled("Temporarily disabled")
	void test02_export() {
		// Assert that we have records with status PENDING.
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING)).isNotEmpty();
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED)).size().isEqualTo(1);

		// Call to load labels because db is not loaded when LabelsProvider.refresh is called in the actual application.
		setupCall()
			.withServicePath(PATH + "/refreshlabels")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Call
		setupCall()
			.withServicePath(PATH + "/caseexporter")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING, FAILED).isEmpty())
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED).size() == 4);
	}

	@Test
	void test03_clean_db() {

		// Assert that we have records with status CREATED and FAILED.
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED)).isNotEmpty();
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, FAILED)).isNotEmpty();
		assertThat(caseMappingRepository.findAll()).isNotEmpty();

		// Call
		setupCall()
			.withServicePath(PATH + "/dbcleaner?from=2024-07-31T12:00:00")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED).isEmpty())
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, FAILED).size() == 1)
			.andVerifyThat(() -> caseMappingRepository.findAll().isEmpty());
	}

	@Test
	void test04_export_when_fail() {

		// Assert that we have records with status PENDING.
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING)).isNotEmpty();

		// Call to load labels because db is not loaded when LabelsProvider.refresh is called in the actual application.
		setupCall()
			.withServicePath(PATH + "/refreshlabels")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Call
		setupCall()
			.withServicePath(PATH + "/caseexporter")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING).isEmpty())
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, FAILED).size() == 3);
	}

	@Test
	@Disabled("Temporarily disabled")
	void test05_export_when_errand_exists() {

		// Assert that we have records with status PENDING.
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING)).isNotEmpty();
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED)).size().isEqualTo(1);

		// Call
		setupCall()
			.withServicePath(PATH + "/caseexporter")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING, FAILED).isEmpty())
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED).size() == 4);
	}

	@Test
	void test06_export_when_attachment_exists() {

		// Assert that we have records with status PENDING.
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING)).isNotEmpty();
		assertThat(repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED)).size().isEqualTo(1);

		// Call to load labels because db is not loaded when LabelsProvider.refresh is called in the actual application.
		setupCall()
			.withServicePath(PATH + "/refreshlabels")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Call
		setupCall()
			.withServicePath(PATH + "/caseexporter")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, PENDING, FAILED).isEmpty())
			.andVerifyThat(() -> repository.findByCaseMetaDataEntityMunicipalityIdAndDeliveryStatusIn(MUNICIPALITY_ID, CREATED).size() == 4);
	}
}
