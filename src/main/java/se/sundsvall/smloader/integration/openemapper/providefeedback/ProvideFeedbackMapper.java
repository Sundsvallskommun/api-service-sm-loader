package se.sundsvall.smloader.integration.openemapper.providefeedback;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.List;

import static generated.se.sundsvall.supportmanagement.Priority.LOW;
import static java.util.Collections.emptyList;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CATEGORY_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TYPE_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class ProvideFeedbackMapper implements OpenEMapper {

	@Value("${lamna-synpunkt.family-id}")
	private String familyId;

	@Override
	public String getSupportedFamilyId() {
		return familyId;
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, ProvideFeedback.class);

		return new Errand()
			.status("NEW")
			.description(result.description())
			.stakeholders(getStakeholder(result))
			.reporterUserId(getReporterUserId(result))
			.priority(LOW)
			.title(result.title())
			.classification(new Classification().category(CATEGORY_LAMNA_SYNPUNKT).type(TYPE_LAMNA_SYNPUNKT))
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false);
	}

	private String getReporterUserId(final ProvideFeedback provideFeedback) {
		return provideFeedback.firstName() != null ? provideFeedback.firstName() + " " + provideFeedback.lastName() + "-" + provideFeedback.email() :
			provideFeedback.posterFirstName() + " " +  provideFeedback.posterLastName() + "-" + provideFeedback.posterEmail();
	}

	private List<Stakeholder> getStakeholder(final ProvideFeedback provideFeedback) {
		return provideFeedback.firstName() != null ? List.of(new Stakeholder().role(ROLE_CONTACT_PERSON)
			.firstName(provideFeedback.firstName())
			.lastName(provideFeedback.lastName())
			.contactChannels(getContactChannels(provideFeedback))) : emptyList();
	}

	private List<ContactChannel> getContactChannels(final ProvideFeedback provideFeedback) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(provideFeedback.email()), new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_PHONE)
			.value(provideFeedback.mobilePhone()));
	}
}
