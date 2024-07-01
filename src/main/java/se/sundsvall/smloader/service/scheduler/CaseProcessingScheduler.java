package se.sundsvall.smloader.service.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.smloader.service.OpenEService;
import se.sundsvall.smloader.service.SupportManagementService;

import java.time.LocalDateTime;

@Service
public class CaseProcessingScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaseProcessingScheduler.class);
	private static final String LOG_IMPORT_STARTED = "Beginning of importing cases";
	private static final String LOG_IMPORT_ENDED = "Import of cases has ended";
	private static final String LOG_EXPORT_STARTED = "Beginning of exporting cases";
	private static final String LOG_EXPORT_ENDED = "Export of cases has ended";

	private final OpenEService openEService;
	private final SupportManagementService supportManagementService;

	public CaseProcessingScheduler(final OpenEService openEService, final SupportManagementService supportManagementService) {
		this.openEService = openEService;
		this.supportManagementService = supportManagementService;
	}

	@Scheduled(cron = "${scheduler.caseprocessing.cron.expression}")
	@SchedulerLock(name = "processcases", lockAtMostFor = "${scheduler.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_IMPORT_STARTED);
		openEService.fetchAndSaveNewOpenECases(LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0), LocalDateTime.now());
		LOGGER.info(LOG_IMPORT_ENDED);

		LOGGER.info(LOG_EXPORT_STARTED);
		supportManagementService.exportCases();
		LOGGER.info(LOG_EXPORT_ENDED);
	}
}
