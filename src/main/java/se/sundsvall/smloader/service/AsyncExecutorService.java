package se.sundsvall.smloader.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Class responsible for async-execution of import/export services.
 *
 * The purpose with this is to detach the execution from the calling thread when the call is initialized from the
 * REST-API.
 */
@Service
public class AsyncExecutorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncExecutorService.class);
	private final OpenEService openEService;
	private final SupportManagementService supportManagementService;
	private final DatabaseCleanerService databaseCleanerService;

	public AsyncExecutorService(final OpenEService openEService, final SupportManagementService supportManagementService, final DatabaseCleanerService databaseCleanerService) {
		this.openEService = openEService;
		this.supportManagementService = supportManagementService;
		this.databaseCleanerService = databaseCleanerService;
	}

	@Async
	public void importCases(final LocalDateTime from, final LocalDateTime to, final String municipalityId) {
		openEService.fetchAndSaveNewOpenECases(from, to, municipalityId, msg -> LOGGER.error("Import: Error on manual run: {}", msg));
	}

	@Async
	public void exportCases(final String municipalityId) {
		supportManagementService.exportCases(municipalityId, msg -> LOGGER.error("Export: Error on manual run: {}", msg));
	}

	@Async
	public void databaseCleanerExecute(final LocalDateTime from, final String municipalityId) {
		final var fromZoned = from.atZone(ZoneId.systemDefault()).toOffsetDateTime();
		databaseCleanerService.cleanDatabase(fromZoned, municipalityId);
	}
}
