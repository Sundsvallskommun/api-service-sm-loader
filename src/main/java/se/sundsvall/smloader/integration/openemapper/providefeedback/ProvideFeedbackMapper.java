package se.sundsvall.smloader.integration.openemapper.providefeedback;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import static se.sundsvall.smloader.integration.util.ErrandConstants.CATEGORY_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
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

		final var email = new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(result.email());

		final var phone = new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_PHONE)
			.value(result.mobilePhone());

		final var stakeholder = new Stakeholder()
			.role(ROLE_CONTACT_PERSON)
			.firstName(result.firstName())
			.lastName(result.lastName())
			.addContactChannelsItem(email)
			.addContactChannelsItem(phone);

		return new Errand()
			.description(result.description())
			.title(result.title())
			.addStakeholdersItem(stakeholder)
			.classification(new Classification().category(CATEGORY_LAMNA_SYNPUNKT).type(TYPE_LAMNA_SYNPUNKT))
			.businessRelated(false);
	}
}
