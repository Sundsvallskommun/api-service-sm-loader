package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

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
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_WORKPLACE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PRECEDENCE_OF_REEMPLOYMENT;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class PrecedenceOfReemploymentMapper implements OpenEMapper {

	private final OpenEMapperProperties properties;

	public PrecedenceOfReemploymentMapper(final @Qualifier("precedenceofreemployment") OpenEMapperProperties properties) {
		this.properties = properties;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, PrecedenceOfReemployment.class);


		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_PRECEDENCE_OF_REEMPLOYMENT)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final PrecedenceOfReemployment precedenceOfReemployment) {
		return List.of(new Stakeholder().role(ROLE_CONTACT_PERSON)
			.firstName(precedenceOfReemployment.posterFirstname())
			.lastName(precedenceOfReemployment.posterLastname())
			.contactChannels(getContactChannels(precedenceOfReemployment.posterEmail(), null)),
			new Stakeholder().role(ROLE_APPLICANT)
				.contactChannels(getContactChannels(precedenceOfReemployment.privateEmail(), precedenceOfReemployment.privatePhone())),
			new Stakeholder().role(ROLE_MANAGER)
				.firstName(precedenceOfReemployment.managerFirstname())
				.lastName(precedenceOfReemployment.managerLastname())
				.contactChannels(getContactChannels(precedenceOfReemployment.managerEmail(), null))
				.organizationName(precedenceOfReemployment.managerOrganization()));
	}

	private List<ContactChannel> getContactChannels(final String email, final String phone) {
		return isNull(phone) ? List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email)) :
			List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email), new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_PHONE)
			.value(phone));
	}

	private List<Parameter> getParameters(final PrecedenceOfReemployment precedenceOfReemployment) {
		var parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter().key(KEY_WORKPLACE).addValuesItem(precedenceOfReemployment.workplace()));
		parameters.add(new Parameter().key(KEY_START_DATE).addValuesItem(precedenceOfReemployment.startDate()));
		Optional.ofNullable(precedenceOfReemployment.position()).ifPresent(position -> parameters.add(new Parameter().key(KEY_POSITION).addValuesItem(position.trim())));

		return parameters;
	}

	private String getReporterUserId(final PrecedenceOfReemployment precedenceOfReemployment) {
		return precedenceOfReemployment.posterFirstname() + " " + precedenceOfReemployment.posterLastname() + "-" + precedenceOfReemployment.posterEmail();
	}
}
