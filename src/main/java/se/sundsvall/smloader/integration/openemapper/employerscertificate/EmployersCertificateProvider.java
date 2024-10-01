package se.sundsvall.smloader.integration.openemapper.employerscertificate;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SEND_DIGITAL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UNEMPLOYMENT_FUND;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_EMPLOYERS_CERTIFICATE;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

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
			.labels(List.of(properties.getLabel()))
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final EmployersCertificate employersCertificate) {
		return List.of(new Stakeholder()
				.role(ROLE_CONTACT_PERSON)
				.firstName(employersCertificate.posterFirstname())
				.lastName(employersCertificate.posterLastname())
				.contactChannels(getContactChannels(employersCertificate.posterEmail())),
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(employersCertificate.applicantFirstname())
				.lastName(employersCertificate.applicantLastname())
				.contactChannels(getContactChannelsForApplicant(employersCertificate.applicantEmail(), employersCertificate.applicantPhone()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(employersCertificate.applicantLegalId())));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private List<ContactChannel> getContactChannelsForApplicant(final String email, final String phone) {
		if (email == null && phone == null) {
			return emptyList();
		}
		return isNull(email) ? List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_PHONE)
			.value(phone)) :
			List.of(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_EMAIL)
				.value(email));
	}

	private List<Parameter> getParameters(final EmployersCertificate employersCertificate) {
		var parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter().key(KEY_UNEMPLOYMENT_FUND).addValuesItem(employersCertificate.unemploymentFund()));
		Optional.ofNullable(employersCertificate.sendDigital()).ifPresent(sendDigital -> parameters.add(new Parameter().key(KEY_SEND_DIGITAL).addValuesItem(sendDigital)));
		Optional.ofNullable(employersCertificate.startDate()).ifPresent(startDate -> parameters.add(new Parameter().key(KEY_START_DATE).addValuesItem(startDate)));
		Optional.ofNullable(employersCertificate.endDate()).ifPresent(endDate -> parameters.add(new Parameter().key(KEY_END_DATE).addValuesItem(endDate)));

		return parameters;
	}

	private String getPartyId(final String legalId) {
		return partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null);
	}

	private String getReporterUserId(final EmployersCertificate employersCertificate) {
		return employersCertificate.posterFirstname() + " " + employersCertificate.posterLastname() + "-" + employersCertificate.posterEmail();
	}
}
