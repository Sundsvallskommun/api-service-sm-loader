package se.sundsvall.smloader.integration.openemapper.substitutemanager;

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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_RESPONSIBILITY_NUMBER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPROVER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_SUBSTITUTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_SUBSTITUTE_MANAGER;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class SubstituteManagerProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;


	public SubstituteManagerProvider(final @Qualifier("substitutemanager") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, SubstituteManager.class);

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_SUBSTITUTE_MANAGER)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(List.of(new Parameter().key(KEY_RESPONSIBILITY_NUMBER).addValuesItem(result.responsibilityNumber()),
				new Parameter().key(KEY_START_DATE).addValuesItem(result.startDate()),
				new Parameter().key(KEY_END_DATE).addValuesItem(result.endDate())))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final SubstituteManager substituteManager) {
		return List.of(new Stakeholder()
			.role(ROLE_CONTACT_PERSON)
			.firstName(substituteManager.posterFirstname())
			.lastName(substituteManager.posterLastname())
			.contactChannels(getContactChannels(substituteManager.posterEmail())),
			new Stakeholder()
				.role(ROLE_SUBSTITUTE)
				.firstName(substituteManager.substituteManagerFirstname())
				.lastName(substituteManager.substituteManagerLastname())
				.organizationName(substituteManager.substituteManagerOrganization())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(substituteManager.substituteManagerLegalId())),
			new Stakeholder()
				.role(ROLE_MANAGER)
				.firstName(substituteManager.managerFirstname())
				.lastName(substituteManager.managerLastname())
				.organizationName(substituteManager.managerOrganization())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(substituteManager.managerLegalId())),
			new Stakeholder()
				.role(ROLE_APPROVER)
				.firstName(substituteManager.approvingManagerFirstname())
				.lastName(substituteManager.approvingManagerLastname())
				.organizationName(substituteManager.approvingManagerOrganization())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(substituteManager.approvingManagerLegalId())));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private String getPartyId(final String legalId) {
		return partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null);
	}

	private String getReporterUserId(final SubstituteManager substituteManager) {
		return !isEmpty(substituteManager.managerUserId()) ? substituteManager.managerUserId() : substituteManager.approvingManagerUserId();
	}
}
