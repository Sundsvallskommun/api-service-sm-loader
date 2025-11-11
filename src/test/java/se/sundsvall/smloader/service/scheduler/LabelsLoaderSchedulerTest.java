package se.sundsvall.smloader.service.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.util.LabelsProvider;

@ExtendWith(MockitoExtension.class)
class LabelsLoaderSchedulerTest {

	@Mock
	private LabelsProvider labelsProviderMock;

	@InjectMocks
	private LabelsLoaderScheduler scheduler;

	@Test
	void execute() {
		scheduler.execute();
		verify(labelsProviderMock).refresh();
		verifyNoMoreInteractions(labelsProviderMock);
	}

}
