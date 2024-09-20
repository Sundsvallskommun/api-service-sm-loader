package se.sundsvall.smloader.integration.openemapper.proposal;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class ProposalMapper implements OpenEMapper {

	private final OpenEMapperProperties properties;

	public ProposalMapper(final @Qualifier("proposal") OpenEMapperProperties properties) {
		this.properties = properties;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, Proposal.class);

		return new Errand()
			.title(result.title())
			.description(result.description())
			.status(STATUS_NEW)
			.reporterUserId(getReporterUserId(result))
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholder(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())));
	}

	private String getReporterUserId(final Proposal proposal) {
		return proposal.firstname() != null ? proposal.firstname() + " " + proposal.lastname() + "-" + proposal.email() :
			proposal.posterFirstname() + " " + proposal.posterLastname() + "-" + proposal.posterEmail();
	}

	private List<Stakeholder> getStakeholder(final Proposal proposal) {
		return proposal.firstname() != null ? List.of(new Stakeholder().role(ROLE_CONTACT_PERSON)
			.firstName(proposal.firstname())
			.lastName(proposal.lastname())
			.address(proposal.address())
			.zipCode(proposal.zipCode())
			.city(proposal.postalAddress())
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
