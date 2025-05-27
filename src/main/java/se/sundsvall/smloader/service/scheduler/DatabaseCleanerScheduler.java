package se.sundsvall.smloader.service.scheduler;

import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.smloader.service.DatabaseCleanerService;
import se.sundsvall.smloader.service.MigrationService;

@Service
public class DatabaseCleanerScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerScheduler.class);
	private static final String LOG_CLEANING_STARTED = "Beginning removal of obsolete entities in the database";
	private static final String LOG_CLEANING_ENDED = "Cleaning of obsolete entities in database has ended";

	@Value("${config.scheduler.keep-days:14}")
	private int keepDays;

	private final DatabaseCleanerService databaseCleanerService;

	// Temporary solution to migrate old data
	private final MigrationService migrationService;

	public DatabaseCleanerScheduler(final DatabaseCleanerService databaseCleanerService, final MigrationService migrationService) {
		this.databaseCleanerService = databaseCleanerService;
		this.migrationService = migrationService;
	}

	@Dept44Scheduled(
		cron = "${scheduler.dbcleaner.cron}",
		name = "${scheduler.dbcleaner.name}",
		lockAtMostFor = "${scheduler.dbcleaner.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.dbcleaner.maximum-execution-time}")
	public void execute() {
		LOGGER.info(LOG_CLEANING_STARTED);
		databaseCleanerService.cleanDatabase(OffsetDateTime.now().minusDays(keepDays), MUNICIPALITY_ID);
		LOGGER.info(LOG_CLEANING_ENDED);
		LOGGER.info("Beginning migration of old data");
		migrationService.migrateReportSick("SALARYANDPENSION", MUNICIPALITY_ID);
		LOGGER.info("Migration of old data has ended");
	}
}
