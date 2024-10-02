package se.sundsvall.smloader.integration.supportmanagement;

import generated.se.sundsvall.supportmanagement.Errand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.smloader.integration.supportmanagement.configuration.SupportManagementConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.smloader.integration.supportmanagement.configuration.SupportManagementConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.support-management.url}", configuration = SupportManagementConfiguration.class)
public interface SupportManagementClient {

	/**
	 * Export errand to support management.
	 *
	 * @param errand with attributes for create an errand.
	 */
	@PostMapping(path = "/{municipalityId}/{namespace}/errands", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> createErrand( @PathVariable(name = "municipalityId") String municipalityId, @PathVariable(name = "namespace") String namespace,
		@RequestBody Errand errand);

	/**
	 * Get errand from support management.
	 *
	 * @param errandId with att.
	 */
	@GetMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	Errand getErrand(@PathVariable(name = "municipalityId") String municipalityId, @PathVariable(name = "namespace") String namespace,
		@PathVariable(name = "errandId") String errandId);

}
