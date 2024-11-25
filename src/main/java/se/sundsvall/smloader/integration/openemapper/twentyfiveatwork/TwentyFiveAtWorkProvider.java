package se.sundsvall.smloader.integration.openemapper.twentyfiveatwork;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_LATEST_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ORIGINAL_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_LATEST_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ORIGINAL_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_TWENTY_FIVE_AT_WORK;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

@Component
class TwentyFiveAtWorkProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	public TwentyFiveAtWorkProvider(final @Qualifier("twentyfiveatwork") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, TwentyFiveAtWork.class);

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_TWENTY_FIVE_AT_WORK)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(properties.getLabels())
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(List.of(new Parameter().key(KEY_LATEST_START_DATE).addValuesItem(result.startDateChangeWork()).displayName(DISPLAY_LATEST_START_DATE),
				new Parameter().key(KEY_ORIGINAL_START_DATE).addValuesItem(result.originalStartDate()).displayName(DISPLAY_ORIGINAL_START_DATE)))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(result.applicantUserId());
	}

	private List<Stakeholder> getStakeholders(final TwentyFiveAtWork twentyFiveAtWork) {
		return List.of(new Stakeholder()
			.role(ROLE_CONTACT_PERSON)
			.firstName(twentyFiveAtWork.posterFirstname())
			.lastName(twentyFiveAtWork.posterLastname())
			.contactChannels(getContactChannels(twentyFiveAtWork.posterEmail())),
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(twentyFiveAtWork.applicantFirstname())
				.lastName(twentyFiveAtWork.applicantLastname())
				.contactChannels(getContactChannels(twentyFiveAtWork.applicantEmail()))
				.metadata(Map.of(KEY_ADMINISTRATION_NAME, twentyFiveAtWork.applicantOrganization()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(twentyFiveAtWork.applicantLegalId())),
			getEmployee(twentyFiveAtWork));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private Stakeholder getEmployee(final TwentyFiveAtWork twentyFiveAtWork) {
		final var stakeholder = new Stakeholder()
			.role(ROLE_EMPLOYEE)
			.firstName(twentyFiveAtWork.firstname())
			.lastName(twentyFiveAtWork.lastname())
			.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
			.externalId(getPartyId(twentyFiveAtWork.legalId()));

		return isNotEmpty(twentyFiveAtWork.otherAddress()) ? stakeholder.address(twentyFiveAtWork.otherAddress())
			.zipCode(twentyFiveAtWork.otherZipCode())
			.city(twentyFiveAtWork.otherPostalAddress())
			: stakeholder.address(twentyFiveAtWork.address())
				.zipCode(twentyFiveAtWork.zipCode())
				.city(twentyFiveAtWork.postalAddress());
	}

	private String getPartyId(final String legalId) {
		return isNotEmpty(legalId) ? partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null) : null;
	}
}
