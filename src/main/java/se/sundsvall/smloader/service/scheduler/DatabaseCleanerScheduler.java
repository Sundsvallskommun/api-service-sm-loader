package se.sundsvall.smloader.service.scheduler;

import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;

import java.time.OffsetDateTime;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.smloader.service.DatabaseCleanerService;

@Service
public class DatabaseCleanerScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerScheduler.class);
	private static final String LOG_CLEANING_STARTED = "Beginning removal of obsolete entities in the database";
	private static final String LOG_CLEANING_ENDED = "Cleaning of obsolete entities in database has ended";

	@Value("${config.scheduler.keep-days:14}")
	private int keepDays;

	private final DatabaseCleanerService databaseCleanerService;

	public DatabaseCleanerScheduler(final DatabaseCleanerService databaseCleanerService) {
		this.databaseCleanerService = databaseCleanerService;
	}

	@Scheduled(cron = "${scheduler.dbcleaner.cron.expression}")
	@SchedulerLock(name = "dbcleaner", lockAtMostFor = "${scheduler.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_CLEANING_STARTED);
		databaseCleanerService.cleanDatabase(OffsetDateTime.now().minusDays(keepDays), MUNICIPALITY_ID);
		LOGGER.info(LOG_CLEANING_ENDED);
	}
}
