package se.sundsvall.smloader.integration.openemapper.permissionorder;

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
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_COMPUTER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_PART_OF_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SYSTEM_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_TYPE_OF_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_USER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PERMISSION_ORDER;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class PermissionOrderProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	public PermissionOrderProvider(final @Qualifier("permissionorder") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, PermissionOrder.class);

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_PERMISSION_ORDER)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(List.of(
				new Parameter().key(KEY_COMPUTER_ID).addValuesItem(result.computerId()),
				new Parameter().key(KEY_ADMINISTRATIVE_UNIT).addValuesItem(result.administrativeUnit()),
				new Parameter().key(KEY_PART_OF_ADMINISTRATIVE_UNIT).addValuesItem(result.partOfAdministrativeUnit()),
				new Parameter().key(KEY_TYPE_OF_ACCESS).addValuesItem(result.typeOfAccess()),
				new Parameter().key(KEY_SYSTEM_ACCESS).addValuesItem(result.systemAccess()),
				new Parameter().key(KEY_START_DATE).addValuesItem(result.startDate())))
			.description(result.otherInformation())
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final PermissionOrder permissionOrder) {
		return List.of(new Stakeholder()
				.role(ROLE_CONTACT_PERSON)
				.firstName(permissionOrder.posterFirstname())
				.lastName(permissionOrder.posterLastname())
				.contactChannels(getContactChannels(permissionOrder.posterEmail())),
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(permissionOrder.applicantFirstname())
				.lastName(permissionOrder.applicantLastname())
				.organizationName(permissionOrder.applicantOrganization()),
			new Stakeholder()
				.role(ROLE_USER)
				.firstName(permissionOrder.userFirstname())
				.lastName(permissionOrder.userLastname())
				.contactChannels(getContactChannels(permissionOrder.userEmail()))
				.organizationName(permissionOrder.userOrganization())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(permissionOrder.userLegalId())));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private String getPartyId(final String legalId) {
		return partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null);
	}

	private String getReporterUserId(final PermissionOrder permissionOrder) {
		return permissionOrder.posterFirstname() + " " + permissionOrder.posterLastname() + "-" + permissionOrder.posterEmail();
	}
}
