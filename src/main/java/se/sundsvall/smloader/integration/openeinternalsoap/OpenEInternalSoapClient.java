package se.sundsvall.smloader.integration.openeinternalsoap;

import generated.se.sundsvall.callback.ConfirmDelivery;
import generated.se.sundsvall.callback.ConfirmDeliveryResponse;
import generated.se.sundsvall.callback.SetStatus;
import generated.se.sundsvall.callback.SetStatusResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.smloader.integration.openeinternalsoap.configuration.OpenEInternalSoapConfiguration;

import static se.sundsvall.smloader.integration.openeinternalsoap.configuration.OpenEInternalSoapConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e.internal-soap.url}", configuration = OpenEInternalSoapConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpenEInternalSoapClient {

	String TEXT_XML_CHARSET_ISO_8859_1 = "text/xml; charset=ISO-8859-1";

	@PostMapping(consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	SetStatusResponse setStatus(@RequestBody SetStatus setStatus);

	@PostMapping(consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	ConfirmDeliveryResponse confirmDelivery(@RequestBody ConfirmDelivery confirmDelivery);

}
