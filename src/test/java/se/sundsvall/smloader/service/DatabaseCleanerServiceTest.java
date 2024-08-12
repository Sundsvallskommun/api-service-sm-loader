package se.sundsvall.smloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;
import se.sundsvall.smloader.integration.db.model.CaseId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerServiceTest {

	@Mock
	private CaseRepository caseRepositoryMock;

	@Mock
	private CaseMappingRepository caseMappingRepositoryMock;

	@InjectMocks
	private DatabaseCleanerService service;

	@Test
	void executeWithEntitiesToRemove() {
		// Setup
		final var entityIdsToRemove = createCaseIds(5);
		final var deleteBefore = OffsetDateTime.now().minusDays(1);

		// Setup mocking
		when(caseRepositoryMock.countByCreatedBeforeAndDeliveryStatusIn(deleteBefore, CREATED, FAILED)).thenReturn(Integer.toUnsignedLong(entityIdsToRemove.size()));
		when(caseRepositoryMock.findIdsByCreatedBeforeAndDeliveryStatusIn(deleteBefore, CREATED, FAILED)).thenReturn(entityIdsToRemove);

		// Call.
		service.cleanDatabase(deleteBefore);

		// Verification.
		verify(caseRepositoryMock).countByCreatedBeforeAndDeliveryStatusIn(deleteBefore, CREATED, FAILED);
		verify(caseRepositoryMock).findIdsByCreatedBeforeAndDeliveryStatusIn(deleteBefore, CREATED, FAILED);
		verify(caseRepositoryMock, times(5)).deleteById(anyString());
		verify(caseMappingRepositoryMock).deleteByModifiedBefore(deleteBefore);
	}

	@Test
	void executeWithNoEntitiesToRemove() {
		// Setup
		final var deleteBefore = OffsetDateTime.now().minusDays(1);

		// Call.
		service.cleanDatabase(deleteBefore);

		// Verification
		verify(caseRepositoryMock).countByCreatedBeforeAndDeliveryStatusIn(deleteBefore, CREATED, FAILED);
		verifyNoMoreInteractions(caseRepositoryMock, caseMappingRepositoryMock);
	}

	private List<CaseId> createCaseIds(final int numberOfCaseIdsToCreate) {
		return IntStream.range(0, numberOfCaseIdsToCreate)
			.mapToObj(i -> createCaseIdInstance(String.valueOf(i)))
			.toList();
	}

	private CaseId createCaseIdInstance(final String  id) {
		return () -> id;
	}
}
