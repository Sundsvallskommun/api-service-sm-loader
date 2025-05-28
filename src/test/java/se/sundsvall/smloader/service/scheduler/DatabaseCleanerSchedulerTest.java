package se.sundsvall.smloader.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.service.DatabaseCleanerService;
import se.sundsvall.smloader.service.MigrationService;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerSchedulerTest {

	@Mock
	private DatabaseCleanerService databaseCleanerServiceMock;

	@Mock
	private MigrationService migrationServiceMock;

	@InjectMocks
	private DatabaseCleanerScheduler scheduler;

	@Test
	void executeWithEntitiesToRemove() {
		scheduler.execute();
		verify(databaseCleanerServiceMock).cleanDatabase(any(OffsetDateTime.class), eq(MUNICIPALITY_ID));
		// Temporary solution to migrate old data
		verify(migrationServiceMock).migrateReportSick(anyString(), eq(MUNICIPALITY_ID));
		verifyNoMoreInteractions(databaseCleanerServiceMock, migrationServiceMock);
	}

}
