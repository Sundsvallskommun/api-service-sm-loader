package se.sundsvall.smloader.integration.messaging;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.SlackRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.smloader.integration.messaging.configuration.MessagingConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.smloader.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.url}", configuration = MessagingConfiguration.class)
public interface MessagingClient {

	@PostMapping("/{municipalityId}/slack")
	ResponseEntity<Void> sendSlack(@PathVariable("municipalityId") final String municipalityId, SlackRequest request);

	@PostMapping(path = "/{municipalityId}/email", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> sendEmail(@PathVariable("municipalityId") final String municipalityId, @RequestBody EmailRequest emailRequest);

}
