package se.sundsvall.smloader.integration.oepintegrator;

import feign.Response;
import generated.se.sundsvall.oepintegrator.CaseEnvelope;
import generated.se.sundsvall.oepintegrator.CaseStatusChangeRequest;
import generated.se.sundsvall.oepintegrator.ConfirmDeliveryRequest;
import generated.se.sundsvall.oepintegrator.InstanceType;
import generated.se.sundsvall.oepintegrator.ModelCase;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.smloader.integration.oepintegrator.configuration.OepIntegratorConfiguration;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.smloader.integration.oepintegrator.configuration.OepIntegratorConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.oep-integrator.url}", configuration = OepIntegratorConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OepIntegratorClient {

	@PostMapping(value = "/{municipalityId}/{instanceType}/cases/{flowInstanceId}/delivery", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> confirmDelivery(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId,
		@RequestBody final ConfirmDeliveryRequest confirmDeliveryRequest);

	@PutMapping(value = "/{municipalityId}/{instanceType}/cases/{flowInstanceId}/status", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> setStatus(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId,
		@RequestBody final CaseStatusChangeRequest setStatusRequest);

	@GetMapping(value = "/{municipalityId}/{instanceType}/cases/families/{familyId}", produces = APPLICATION_JSON_VALUE)
	List<CaseEnvelope> getCases(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("familyId") final int familyId,
		@RequestParam(value = "fromDate", required = false) final String fromDate,
		@RequestParam(value = "toDate", required = false) final String toDate,
		@RequestParam(value = "status", required = false) final String status);

	@GetMapping(value = "/{municipalityId}/{instanceType}/cases/{flowInstanceId}", produces = APPLICATION_JSON_VALUE)
	ModelCase getCase(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId);

	@GetMapping(path = "/{municipalityId}/{instanceType}/cases/{flowInstanceId}/queries/{queryId}/files/{fileId}")
	Response getAttachment(
		@PathVariable final String municipalityId,
		@PathVariable final InstanceType instanceType,
		@PathVariable final String flowInstanceId,
		@PathVariable final String queryId,
		@PathVariable final String fileId);

}
