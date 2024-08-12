package se.sundsvall.smloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AsyncExecutorServiceTest {

	@Mock
	private OpenEService openEServiceMock;

	@Mock
	private SupportManagementService supportManagementServiceMock;

	@Mock
	private DatabaseCleanerService databaseCleanerServiceMock;

	@InjectMocks
	private AsyncExecutorService asyncExecutorService;

	@Test
	void importCases() {
		final var from = LocalDateTime.now().minusDays(7);
		final var to = LocalDateTime.now();

		// Call
		asyncExecutorService.importCases(from, to);

		verify(openEServiceMock).fetchAndSaveNewOpenECases(from, to);
		verifyNoInteractions(supportManagementServiceMock);
	}

	@Test
	void exportCases() {

		// Call
		asyncExecutorService.exportCases();

		verify(supportManagementServiceMock).exportCases();
		verifyNoInteractions(openEServiceMock);
	}

	@Test
	void databaseCleanerExecute() {
		final var from = LocalDateTime.now().minusDays(7);

		// Call
		asyncExecutorService.databaseCleanerExecute(from);

		verify(databaseCleanerServiceMock).cleanDatabase(any(OffsetDateTime.class));
		verifyNoInteractions(openEServiceMock);
		verifyNoInteractions(supportManagementServiceMock);
	}
}
