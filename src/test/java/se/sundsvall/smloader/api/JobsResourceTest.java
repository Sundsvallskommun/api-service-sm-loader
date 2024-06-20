package se.sundsvall.smloader.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.smloader.service.AsyncExecutorService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class JobsResourceTest {
	private static final String PATH = "/jobs";

	@MockBean
	private AsyncExecutorService asyncExecutorService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void caseExporter() {

		// Call
		webTestClient.post().uri(PATH + "/caseexporter")
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService).exportCases();
		verify(asyncExecutorService, never()).databaseCleanerExecute(any(LocalDateTime.class));
		verify(asyncExecutorService, never()).importCases(any(LocalDateTime.class), any(LocalDateTime.class));
	}

	@Test
	void caseImporter() {
		final var from = LocalDateTime.now().minusDays(7);
		final var to = LocalDateTime.now();

		// Call
		webTestClient.post().uri(uriBuilder -> uriBuilder.path(PATH + "/caseimporter")
			.queryParam("from", from.toString())
			.queryParam("to", to.toString())
				.build())
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService).importCases(from, to);
		verify(asyncExecutorService, never()).exportCases();
		verify(asyncExecutorService, never()).databaseCleanerExecute(any(LocalDateTime.class));
	}

	@Test
	void dbCleaner() {
		final var from = LocalDateTime.now().minusDays(7);

		// Call
		webTestClient.post().uri(uriBuilder -> uriBuilder.path(PATH + "/dbcleaner")
				.queryParam("from", from.toString())
				.build())
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService).databaseCleanerExecute(from);
		verify(asyncExecutorService, never()).importCases(any(LocalDateTime.class), any(LocalDateTime.class));
		verify(asyncExecutorService, never()).exportCases();
	}
}
