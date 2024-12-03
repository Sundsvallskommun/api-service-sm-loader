package se.sundsvall.smloader.integration.openemapper.employerscertificate;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_CONSENT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_DELIVERY_METHOD;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SEND_DIGITAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_TIME_PERIOD;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_UNEMPLOYMENT_FUND;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CONSENT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_DELIVERY_METHOD;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SEND_DIGITAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_TIME_PERIOD;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UNEMPLOYMENT_FUND;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_EMPLOYERS_CERTIFICATE;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

@Component
class EmployersCertificateProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	public EmployersCertificateProvider(final @Qualifier("employerscertificate") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, EmployersCertificate.class);

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_EMPLOYERS_CERTIFICATE)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(properties.getLabels())
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final EmployersCertificate employersCertificate) {
		return List.of(new Stakeholder()
			.role(ROLE_CONTACT_PERSON)
			.firstName(employersCertificate.posterFirstname())
			.lastName(employersCertificate.posterLastname())
			.contactChannels(getContactChannels(employersCertificate.posterEmail())),
			getApplicant(employersCertificate));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private List<ContactChannel> getContactChannelsForApplicant(final String email, final String phone) {
		if ((email == null) && (phone == null)) {
			return emptyList();
		}
		return isNull(email) ? List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_PHONE)
			.value(phone))
			: List.of(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_EMAIL)
				.value(email));
	}

	private List<Parameter> getParameters(final EmployersCertificate employersCertificate) {
		final var parameters = new ArrayList<Parameter>();
		Optional.ofNullable(employersCertificate.sendDigital()).ifPresent(sendDigital -> parameters.add(new Parameter().key(KEY_SEND_DIGITAL).addValuesItem(sendDigital)
			.displayName(DISPLAY_SEND_DIGITAL)));
		Optional.ofNullable(employersCertificate.startDate()).ifPresent(startDate -> parameters.add(new Parameter().key(KEY_START_DATE).addValuesItem(startDate)
			.displayName(DISPLAY_START_DATE)));
		Optional.ofNullable(employersCertificate.endDate()).ifPresent(endDate -> parameters.add(new Parameter().key(KEY_END_DATE).addValuesItem(endDate)
			.displayName(DISPLAY_END_DATE)));
		Optional.ofNullable(employersCertificate.timePeriod()).ifPresent(timePeriod -> parameters.add(new Parameter().key(KEY_TIME_PERIOD).addValuesItem(timePeriod)
			.displayName(DISPLAY_TIME_PERIOD)));
		Optional.ofNullable(employersCertificate.consent()).ifPresent(consent -> parameters.add(new Parameter().key(KEY_CONSENT).addValuesItem(consent)
			.displayName(DISPLAY_CONSENT)));
		Optional.ofNullable(employersCertificate.deliveryMethod()).ifPresent(consent -> parameters.add(new Parameter().key(KEY_DELIVERY_METHOD).addValuesItem(consent)
			.displayName(DISPLAY_DELIVERY_METHOD)));
		Optional.ofNullable(employersCertificate.unemploymentFund()).ifPresent(unemploymentFund -> parameters.add(new Parameter().key(KEY_UNEMPLOYMENT_FUND).addValuesItem(unemploymentFund)
			.displayName(DISPLAY_UNEMPLOYMENT_FUND)));

		return parameters;
	}

	private Stakeholder getApplicant(final EmployersCertificate employersCertificate) {
		final var stakeholder = new Stakeholder()
			.role(ROLE_APPLICANT)
			.firstName(employersCertificate.applicantFirstname())
			.lastName(employersCertificate.applicantLastname())
			.contactChannels(getContactChannelsForApplicant(employersCertificate.applicantEmail(), employersCertificate.applicantPhone()))
			.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
			.externalId(getPartyId(employersCertificate.applicantLegalId()));

		return isNotEmpty(employersCertificate.alternativeAddress()) ? stakeholder.address(employersCertificate.alternativeAddress())
			.zipCode(employersCertificate.alternativeZipCode())
			.city(employersCertificate.alternativePostalAddress())
			: stakeholder.address(employersCertificate.applicantAddress())
				.zipCode(employersCertificate.applicantZipCode())
				.city(employersCertificate.applicantPostalAddress());
	}

	private String getPartyId(final String legalId) {
		return isNotEmpty(legalId) ? partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null) : null;
	}

	private String getReporterUserId(final EmployersCertificate employersCertificate) {
		return employersCertificate.posterFirstname() + " " + employersCertificate.posterLastname() + "-" + employersCertificate.posterEmail();
	}
}
