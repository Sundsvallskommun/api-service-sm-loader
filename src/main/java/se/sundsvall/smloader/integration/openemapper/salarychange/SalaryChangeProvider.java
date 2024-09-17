package se.sundsvall.smloader.integration.openemapper.salarychange;

import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.List;
import java.util.Set;

import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_AMOUNT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FROM_MONTH;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class SalaryChangeProvider implements OpenEMapper {
	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	public SalaryChangeProvider(final @Qualifier("salarychange") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, SalaryChange.class);

		return new Errand()
			.status(STATUS_NEW)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(List.of(new Parameter().key(KEY_AMOUNT).addValuesItem(result.amount()),
				new Parameter().key(KEY_FROM_MONTH).addValuesItem(result.fromMonth())))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())));
	}

	private List<Stakeholder> getStakeholders(final SalaryChange salaryChange) {
		return List.of(new Stakeholder()
				.role(ROLE_CONTACT_PERSON)
				.firstName(salaryChange.posterFirstname())
				.lastName(salaryChange.posterLastname())
				.contactChannels(getContactChannels(salaryChange.posterEmail())),
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(salaryChange.applicantFirstname())
				.lastName(salaryChange.applicantLastname())
				.organizationName(salaryChange.applicantOrganization())
				.contactChannels(getContactChannels(salaryChange.applicantEmail()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(salaryChange.applicantLegalId())));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private String getPartyId(final String legalId) {
		return partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null);
	}
}
