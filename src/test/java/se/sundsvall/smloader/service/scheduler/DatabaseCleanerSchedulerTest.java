package se.sundsvall.smloader.service.scheduler;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.service.DatabaseCleanerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerSchedulerTest {

	@Mock
	private DatabaseCleanerService databaseCleanerServiceMock;

	@InjectMocks
	private DatabaseCleanerScheduler scheduler;

	@Test
	void executeWithEntitiesToRemove() {
		scheduler.execute();
		verify(databaseCleanerServiceMock).cleanDatabase(any(OffsetDateTime.class), eq(MUNICIPALITY_ID));
		verifyNoMoreInteractions(databaseCleanerServiceMock);
	}

}
