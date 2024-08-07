package se.sundsvall.smloader.integration.openemapper.proposal;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.List;

import static java.util.Collections.emptyList;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class ProposalMapper implements OpenEMapper {

	@Value("${sundsvallsforslaget.family-id}")
	private String familyId;

	@Value("${sundsvallsforslaget.category}")
	private String category;

	@Value("${sundsvallsforslaget.type}")
	private String type;

	@Value("${sundsvallsforslaget.priority}")
	private String priority;

	@Override
	public String getSupportedFamilyId() {
		return familyId;
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, Proposal.class);

		return new Errand()
			.title(result.title())
			.description(result.description())
			.status("NEW")
			.reporterUserId(getReporterUserId(result))
			.priority(Priority.fromValue(priority))
			.stakeholders(getStakeholder(result))
			.classification(new Classification().category(category).type(type))
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false);
	}

	private String getReporterUserId(final Proposal proposal) {
		return proposal.firstName() != null ? proposal.firstName() + " " + proposal.lastName() + "-" + proposal.email() :
			proposal.posterFirstName() + " " + proposal.posterLastName() + "-" + proposal.posterEmail();
	}

	private List<Stakeholder> getStakeholder(final Proposal proposal) {
		return proposal.firstName() != null ? List.of(new Stakeholder().role(ROLE_CONTACT_PERSON)
			.firstName(proposal.firstName())
			.lastName(proposal.lastName())
			.address(proposal.address())
			.zipCode(proposal.zipCode())
			.contactChannels(getContactChannels(proposal))) : emptyList();
	}

	private List<ContactChannel> getContactChannels(final Proposal proposal) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(proposal.email()), new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_PHONE)
			.value(proposal.mobilePhone()));
	}
}
