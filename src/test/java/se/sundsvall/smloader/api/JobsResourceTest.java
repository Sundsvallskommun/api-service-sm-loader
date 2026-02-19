package se.sundsvall.smloader.api;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.smloader.service.AsyncExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class JobsResourceTest {
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/jobs";

	@MockitoBean
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
		verify(asyncExecutorService).exportCases(anyString());
		verify(asyncExecutorService, never()).databaseCleanerExecute(any(LocalDateTime.class), anyString());
		verify(asyncExecutorService, never()).importCases(any(LocalDateTime.class), any(LocalDateTime.class), anyString());
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
		verify(asyncExecutorService).importCases(from, to, MUNICIPALITY_ID);
		verify(asyncExecutorService, never()).exportCases(anyString());
		verify(asyncExecutorService, never()).databaseCleanerExecute(any(LocalDateTime.class), anyString());
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
		verify(asyncExecutorService).databaseCleanerExecute(from, MUNICIPALITY_ID);
		verify(asyncExecutorService, never()).importCases(any(LocalDateTime.class), any(LocalDateTime.class), anyString());
		verify(asyncExecutorService, never()).exportCases(anyString());
	}

	@Test
	void refreshLabels() {

		// Call
		webTestClient.post().uri(uriBuilder -> uriBuilder.path(PATH + "/labels/refresh")
			.build())
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService).refreshLabels();
		verify(asyncExecutorService, never()).importCases(any(LocalDateTime.class), any(LocalDateTime.class), anyString());
		verify(asyncExecutorService, never()).exportCases(anyString());
		verify(asyncExecutorService, never()).databaseCleanerExecute(any(LocalDateTime.class), anyString());
	}
}
