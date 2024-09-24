package se.sundsvall.smloader.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Class responsible for async-execution of import/export services.
 *
 * The purpose with this is to detach the execution from the calling thread
 * when the call is initialized from the REST-API.
 */
@Service
public class AsyncExecutorService {

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
		openEService.fetchAndSaveNewOpenECases(from, to, municipalityId);
	}

	@Async
	public void exportCases(final String municipalityId) {
		supportManagementService.exportCases(municipalityId);
	}

	@Async
	public void databaseCleanerExecute(final LocalDateTime from, final String municipalityId) {
		final var fromZoned = from.atZone(ZoneId.systemDefault()).toOffsetDateTime();
		databaseCleanerService.cleanDatabase(fromZoned, municipalityId);
	}
}
