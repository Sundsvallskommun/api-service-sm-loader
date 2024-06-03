package se.sundsvall.smloader.integration.openemapper.lamnasynpunkt;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.util.XPathUtil;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import static se.sundsvall.smloader.integration.util.ErrandConstants.CATEGORY_LAMNA_SYNPUNKT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TYPE_LAMNA_SYNPUNKT;

@Component
class LamnaSynpunktMapper implements OpenEMapper {

	@Value("${lamna-synpunkt.family-id}")
	private String familyId;

	@Override
	public String getSupportedFamilyId() {
		return familyId;
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = XPathUtil.extractValue(xml, LamnaSynpunkt.class);

		final var email = new ContactChannel()
		.type(CONTACT_CHANNEL_TYPE_EMAIL)
		.value(result.epost());

		final var phone = new ContactChannel()
		.type(CONTACT_CHANNEL_TYPE_PHONE)
		.value(result.mobilnummer());

		final var stakeholder = new Stakeholder()
		.role(ROLE_CONTACT_PERSON)
		.firstName(result.fornamn())
		.lastName(result.efternamn())
		.addContactChannelsItem(email)
		.addContactChannelsItem(phone);

		return new Errand()
			.description(result.beskrivning())
			.title(result.rubrik())
			.addStakeholdersItem(stakeholder)
			.classification(new Classification().category(CATEGORY_LAMNA_SYNPUNKT).type(TYPE_LAMNA_SYNPUNKT))
			.businessRelated(false);
	}
}
