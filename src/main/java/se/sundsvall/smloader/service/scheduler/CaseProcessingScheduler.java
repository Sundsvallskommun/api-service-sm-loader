package se.sundsvall.smloader.service.scheduler;

import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.smloader.service.OpenEService;
import se.sundsvall.smloader.service.SupportManagementService;

@Service
public class CaseProcessingScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaseProcessingScheduler.class);
	private static final String LOG_IMPORT_STARTED = "Beginning of importing cases";
	private static final String LOG_IMPORT_ENDED = "Import of cases has ended";
	private static final String LOG_EXPORT_STARTED = "Beginning of exporting cases";
	private static final String LOG_EXPORT_ENDED = "Export of cases has ended";

	@Value("${config.scheduler.fetch-days:1}")
	private int daysToFetch;
	@Value("${scheduler.caseprocessing.name}")
	private String jobName;

	private final OpenEService openEService;
	private final SupportManagementService supportManagementService;
	private final Dept44HealthUtility dept44HealthUtility;

	public CaseProcessingScheduler(final OpenEService openEService, final SupportManagementService supportManagementService, final Dept44HealthUtility dept44HealthUtility) {
		this.openEService = openEService;
		this.supportManagementService = supportManagementService;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	@Dept44Scheduled(
		cron = "${scheduler.caseprocessing.cron}",
		name = "${scheduler.caseprocessing.name}",
		lockAtMostFor = "${scheduler.caseprocessing.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.caseprocessing.maximum-execution-time}")
	public void execute() {

		Consumer<String> importHealthConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, String.format("Import error: %s", msg));
		Consumer<String> exportHealthConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, String.format("Export error: %s", msg));

		LOGGER.info(LOG_IMPORT_STARTED);
		openEService.fetchAndSaveNewOpenECases(LocalDateTime.now().minusDays(daysToFetch).withHour(0).withMinute(0).withSecond(0).withNano(0), LocalDateTime.now(), MUNICIPALITY_ID, importHealthConsumer);
		LOGGER.info(LOG_IMPORT_ENDED);

		LOGGER.info(LOG_EXPORT_STARTED);
		supportManagementService.exportCases(MUNICIPALITY_ID, exportHealthConsumer);
		LOGGER.info(LOG_EXPORT_ENDED);
	}
}
