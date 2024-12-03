package se.sundsvall.smloader.integration.party;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.smloader.integration.party.configuration.PartyConfiguration.CLIENT_ID;

import generated.se.sundsvall.party.PartyType;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.smloader.integration.party.configuration.PartyConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.party.url}", configuration = PartyConfiguration.class, dismiss404 = true)
public interface PartyClient {

	/**
	 * Get partyId by type and legal-ID.
	 *
	 * @param  municipalityId                       the municipality ID.
	 * @param  partyType                            the type of party.
	 * @param  legalId                              the legal-ID.
	 * @return                                      an optional string containing the partyId that corresponds to the
	 *                                              provided partyType and legalId.
	 * @throws org.zalando.problem.ThrowableProblem
	 */
	@GetMapping(path = "/{municipalityId}/{type}/{legalId}/partyId", produces = TEXT_PLAIN_VALUE)
	Optional<String> getPartyId(@PathVariable("municipalityId") String municipalityId, @PathVariable("type") PartyType partyType, @PathVariable("legalId") String legalId);

}
