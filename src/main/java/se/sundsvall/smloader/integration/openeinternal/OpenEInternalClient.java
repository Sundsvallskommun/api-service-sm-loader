package se.sundsvall.smloader.integration.openeinternal;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.smloader.integration.openeinternal.configuration.OpenEInternalConfiguration;

import static se.sundsvall.smloader.integration.openeinternal.configuration.OpenEInternalConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e-internal.url}", configuration = OpenEInternalConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpenEInternalClient {

	String TEXT_XML_CHARSET_ISO_8859_1 = "text/xml; charset=ISO-8859-1";

	@GetMapping(path = "/api/instanceapi/getinstances/family/{familyId}/{status}", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrandIds(@PathVariable(name = "familyId") final String familyId,
		@PathVariable(name = "status") final String status,
		@RequestParam(name = "fromDate") final String fromDate,
		@RequestParam(name = "toDate") final String toDate);

	@GetMapping(path = "/api/instanceapi/getinstance/{flowInstanceId}/xml", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrand(@PathVariable(name = "flowInstanceId") String flowInstanceId);

	@GetMapping(path = "/api/fileuploadqueryapi/getFile/{flowInstanceId}/{queryId}/{fileId}", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getFile(@PathVariable(name = "flowInstanceId") String flowInstanceId, @PathVariable(name = "queryId") String queryId,
		@PathVariable(name = "fileId") String fileId);

}
