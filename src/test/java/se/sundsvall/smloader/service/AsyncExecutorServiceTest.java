package se.sundsvall.smloader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		final var municipalityId = "municipalityId";

		// Call
		asyncExecutorService.importCases(from, to, municipalityId);

		verify(openEServiceMock).fetchAndSaveNewOpenECases(from, to, municipalityId);
		verifyNoInteractions(supportManagementServiceMock);
	}

	@Test
	void exportCases() {
		final var municipalityId = "municipalityId";

		// Call
		asyncExecutorService.exportCases(municipalityId);

		verify(supportManagementServiceMock).exportCases(municipalityId);
		verifyNoInteractions(openEServiceMock);
	}

	@Test
	void databaseCleanerExecute() {
		final var from = LocalDateTime.now().minusDays(7);
		final var municipalityId = "municipalityId";

		// Call
		asyncExecutorService.databaseCleanerExecute(from, municipalityId);

		verify(databaseCleanerServiceMock).cleanDatabase(any(OffsetDateTime.class), anyString());
		verifyNoInteractions(openEServiceMock);
		verifyNoInteractions(supportManagementServiceMock);
	}
}
