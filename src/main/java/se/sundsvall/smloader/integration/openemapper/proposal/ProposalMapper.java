package se.sundsvall.smloader.integration.openemapper.proposal;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import static generated.se.sundsvall.supportmanagement.Priority.LOW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CATEGORY_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TYPE_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@ExcludeFromJacocoGeneratedCoverageReport
@Component
class ProposalMapper implements OpenEMapper {

	@Value("${sundsvallsforslaget.family-id}")
	private String familyId;

	@Override
	public String getSupportedFamilyId() {
		return familyId;
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, Proposal.class);

		return new Errand()
			.status("NEW")
			.reporterUserId(getReporterUserId(result))
			.priority(LOW)
			//TODO: Check category and type
			.classification(new Classification().category(CATEGORY_LAMNA_SYNPUNKT).type(TYPE_LAMNA_SYNPUNKT))
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false);
	}

	private String getReporterUserId(final Proposal proposal) {
		return proposal.posterFirstName() + " " +  proposal.posterLastName() + "-" + proposal.posterEmail();
	}
}
