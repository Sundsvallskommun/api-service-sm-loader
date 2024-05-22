package se.sundsvall.smloader.integration.openeexternalsoap;

import callback.ConfirmDelivery;
import callback.ConfirmDeliveryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.smloader.integration.openeexternalsoap.configuration.OpenEExternalSoapConfiguration;

import static se.sundsvall.smloader.integration.openeexternalsoap.configuration.OpenEExternalSoapConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e-external-soap.url}", configuration = OpenEExternalSoapConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpenEExternalSoapClient {

	String TEXT_XML_UTF_8 = "text/xml; charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF_8, produces = TEXT_XML_UTF_8)
	ConfirmDeliveryResponse confirmDelivery(@RequestBody ConfirmDelivery confirmDelivery);

}
