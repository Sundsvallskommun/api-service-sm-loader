package se.sundsvall.smloader.integration.openemapper.contactsalaryandpension;

import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_USER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class ContactSalaryAndPensionProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;


	public ContactSalaryAndPensionProvider(final @Qualifier("contactsalaryandpension") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, ContactSalaryAndPension.class);

		final var users = parseUsers(xml);

		return new Errand()
			.status(STATUS_NEW)
			.priority(Priority.fromValue(properties.getPriority()))
			.title(result.subject())
			.description(result.description())
			.stakeholders(getStakeholders(result, users))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final ContactSalaryAndPension contactSalaryAndPension, final List<User> users) {
		final var stakeholders = new ArrayList<Stakeholder>();

		stakeholders.add(new Stakeholder()
			.role(ROLE_CONTACT_PERSON)
			.firstName(contactSalaryAndPension.posterFirstname())
			.lastName(contactSalaryAndPension.posterLastname())
			.contactChannels(getContactChannels(contactSalaryAndPension.posterEmail())));

		if (contactSalaryAndPension.contactFirstname() != null) {
			stakeholders.add(new Stakeholder()
				.role(ROLE_CONTACT_PERSON)
				.firstName(contactSalaryAndPension.contactFirstname())
				.lastName(contactSalaryAndPension.contactLastname())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(contactSalaryAndPension.contactLegalId()))
				.organizationName(contactSalaryAndPension.contactOrganization()));
		}

		users.forEach(user ->
			stakeholders.add(new Stakeholder()
				.role(ROLE_USER)
				.firstName(user.firstname())
				.lastName(user.lastname())
				.contactChannels(getContactChannels(user.email()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(user.legalId())))
		);

		return stakeholders;
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private String getPartyId(final String legalId) {
		return partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null);
	}

	public static List<User> parseUsers(byte[] xml) {
		final var elements = evaluateXPath(xml, "/FlowInstance/Values/personalIdentityNumbers/User");

		final var users = new ArrayList<User>();

		elements.forEach(element ->
			users.add(new User(
							evaluateXPath(element, "/Username").text(),
							evaluateXPath(element, "/Firstname").text(),
							evaluateXPath(element, "/Lastname").text(),
							evaluateXPath(element, "/CitizenIdentifier").text(),
							evaluateXPath(element, "/Email").text()))
		);

		return users;
	}

	private String getReporterUserId(final ContactSalaryAndPension contactSalaryAndPension) {
		return contactSalaryAndPension.posterFirstname() + " " + contactSalaryAndPension.posterLastname() + "-" + contactSalaryAndPension.posterEmail();
	}
}
