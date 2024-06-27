package se.sundsvall.smloader.integration.openeexternalsoap;

import generated.se.sundsvall.callback.ConfirmDelivery;
import generated.se.sundsvall.callback.ConfirmDeliveryResponse;
import generated.se.sundsvall.callback.SetStatus;
import generated.se.sundsvall.callback.SetStatusResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.smloader.integration.openeexternalsoap.configuration.OpenEExternalSoapConfiguration;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static se.sundsvall.smloader.integration.openeexternalsoap.configuration.OpenEExternalSoapConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e.external-soap.url}", configuration = OpenEExternalSoapConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpenEExternalSoapClient {

	@PostMapping(consumes = APPLICATION_XML_VALUE, produces = APPLICATION_XML_VALUE)
	SetStatusResponse setStatus(@RequestBody SetStatus setStatus);

	@PostMapping(consumes = APPLICATION_XML_VALUE, produces = APPLICATION_XML_VALUE)
	ConfirmDeliveryResponse confirmDelivery(@RequestBody ConfirmDelivery confirmDelivery);

}
