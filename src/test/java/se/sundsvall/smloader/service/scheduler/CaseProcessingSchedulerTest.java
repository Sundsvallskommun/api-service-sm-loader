package se.sundsvall.smloader.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.service.OpenEService;
import se.sundsvall.smloader.service.SupportManagementService;

@ExtendWith(MockitoExtension.class)
class CaseProcessingSchedulerTest {

	@Mock
	private OpenEService openEServiceMock;

	@Mock
	private SupportManagementService supportManagementServiceMock;

	@InjectMocks
	private CaseProcessingScheduler service;

	@Test
	void exportCases() {
		service.execute();
		verify(openEServiceMock).fetchAndSaveNewOpenECases(any(LocalDateTime.class), any(LocalDateTime.class), anyString(), any());
		verify(supportManagementServiceMock).exportCases(eq("2281"), any());
		verifyNoMoreInteractions(openEServiceMock, supportManagementServiceMock);
	}
}
