package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

import static java.util.Objects.isNull;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_LAST_DAY_OF_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SALARY_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_WORKPLACE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_LAST_DAY_OF_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SALARY_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_WORKPLACE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PRECEDENCE_OF_REEMPLOYMENT;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

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
import se.sundsvall.smloader.service.mapper.OpenEMapper;

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
			.labels(List.of(properties.getCategory(), properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final PrecedenceOfReemployment precedenceOfReemployment) {
		return List.of(new Stakeholder().role(ROLE_CONTACT_PERSON)
			.firstName(precedenceOfReemployment.posterFirstname())
			.lastName(precedenceOfReemployment.posterLastname())
			.contactChannels(getContactChannels(precedenceOfReemployment.posterEmail(), null)),
			new Stakeholder().role(ROLE_APPLICANT)
				.firstName(precedenceOfReemployment.applicantFirstname())
				.lastName(precedenceOfReemployment.applicantLastname())
				.address(precedenceOfReemployment.applicantAddress())
				.zipCode(precedenceOfReemployment.applicantZipCode())
				.city(precedenceOfReemployment.applicantPostalAddress())
				.contactChannels(getContactChannels(precedenceOfReemployment.applicantEmail(), precedenceOfReemployment.applicantPhone())));
	}

	private List<ContactChannel> getContactChannels(final String email, final String phone) {
		return isNull(phone) ? List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email))
			: List.of(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_EMAIL)
				.value(email), new ContactChannel()
					.type(CONTACT_CHANNEL_TYPE_PHONE)
					.value(phone));
	}

	private List<Parameter> getParameters(final PrecedenceOfReemployment precedenceOfReemployment) {
		final var parameters = new ArrayList<Parameter>();
		Optional.ofNullable(precedenceOfReemployment.workplace()).ifPresent(workplace -> parameters.add(new Parameter().key(KEY_WORKPLACE).addValuesItem(workplace)
			.displayName(DISPLAY_WORKPLACE)));
		Optional.ofNullable(precedenceOfReemployment.lastDayOfPosition()).ifPresent(lastDay -> parameters.add(new Parameter().key(KEY_LAST_DAY_OF_POSITION).addValuesItem(lastDay)
			.displayName(DISPLAY_LAST_DAY_OF_POSITION)));
		Optional.ofNullable(precedenceOfReemployment.position()).ifPresent(position -> parameters.add(new Parameter().key(KEY_POSITION).addValuesItem(position.trim())
			.displayName(DISPLAY_POSITION)));
		Optional.ofNullable(precedenceOfReemployment.salaryType()).ifPresent(salaryType -> parameters.add(new Parameter().key(KEY_SALARY_TYPE).addValuesItem(salaryType.trim())
			.displayName(DISPLAY_SALARY_TYPE)));
		Optional.ofNullable(precedenceOfReemployment.manager()).ifPresent(manager -> parameters.add(new Parameter().key(KEY_MANAGER).addValuesItem(manager)
			.displayName(DISPLAY_MANAGER)));

		return parameters;
	}

	private String getReporterUserId(final PrecedenceOfReemployment precedenceOfReemployment) {
		return precedenceOfReemployment.posterFirstname() + " " + precedenceOfReemployment.posterLastname() + "-" + precedenceOfReemployment.posterEmail();
	}
}
