package se.sundsvall.smloader.integration.openemapper.permissionorder;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_USER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PERMISSION_ORDER;

@ExtendWith(MockitoExtension.class)
class PermissionOrderProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@InjectMocks
	private PermissionOrderProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("101");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("101");
	}

	@Test
	void mapToErrand() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		var stringBytes = readOpenEFile("flow-instance-ny-behorighet.xml");

		// Act
		var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_PERMISSION_ORDER);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(8).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactlyInAnyOrder(
			tuple("computerId", List.of("WB12345"), "Dator-id"),
			tuple("isManager", List.of("Nej"), "Är chef eller ej"),
			tuple("notEmployee", List.of("Personen ska inte ha anställning i kommunen utan har annan typ av förordnande"), "Anställning"),
			tuple("administrativeUnit", List.of("Överförmyndarkontoret"), "Förvaltning/verksamhet"),
			tuple("administrativeUnitPartOfK", List.of("Hela förvaltningen"), "Del av förvaltning OfK"),
			tuple("typeOfAccess", List.of("Ny"), "Typ av behörighet"),
			tuple("systemAccess", List.of("Rapp82"), "Systembehörighet"),
			tuple("startDate", List.of("2024-09-04"), "Startdatum"));

		assertThat(errand.getStakeholders()).hasSize(4).
			extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null),
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab", null, null),
				tuple(ROLE_MANAGER, "Jocke", "Anka", List.of(new ContactChannel().type("Email").value("jocke.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab", null, null),
				tuple(ROLE_USER, "Knatte", "Anka", List.of(new ContactChannel().type("Email").value("knatte.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab",  "PRIVATE", partyId));

		assertThat(errand.getExternalTags()).hasSize(1).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6850")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		verify(partyClient).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}

	@Test
	void mapToErrandManyUnits() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		var stringBytes = readOpenEFile("flow-instance-ny-behorighet-flera-förvaltningar.xml");

		// Act
		var errand = provider.mapToErrand(stringBytes);

		var administrativeUnit = List.of("Barn och utbildningsförvaltningen", "Kultur och fritidsförvaltningen", "Vård och omsorgsförvaltningen",
			"Individ och arbetsmarknadsförvaltningen", "Kommunstyrelsekontoret",  "Stadsbyggnadskontoret", "Lantmäterikontoret",
			"Miljökontoret",  "Överförmyndarkontoret");
		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_PERMISSION_ORDER);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(24).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactlyInAnyOrder(
			tuple("computerId", List.of("WB22334"), "Dator-id"),
			tuple("isManager", List.of("Ja, jag är chef och har rätt att godkänna beställningen"), "Är chef eller ej"),
			tuple("notEmployee", List.of("Personen ska inte ha anställning i kommunen utan har annan typ av förordnande"), "Anställning"),
			tuple("typeOfAccess", List.of("Ny"), "Typ av behörighet"),
			tuple("systemAccess", List.of("Heroma"), "Systembehörighet"),
			tuple("userTypeHeroma", List.of("Medarbetare"), "Användartyp Heroma"),
			tuple("uppdateDescription", List.of("Testar"), "Uppdateringsbeskrivning"),
			tuple("stillEmployed", List.of("JA"), "Fortfarande kommunanställd"),
			tuple("administrativeUnit", administrativeUnit, "Förvaltning/verksamhet"),
			tuple("administrativeUnitPartBoU", List.of("Förskola/Grundskola/Gymnasium"), "Del av förvaltning BoU"),
			tuple("unitBoU", List.of("Testskola 1"), "Skola/enhet BoU"),
			tuple("administrativeUnitPartKoF", List.of("Specifik enhet"), "Del av förvaltning KoF"),
			tuple("unitKoF", List.of("Test"), "Enhet KoF"),
			tuple("administrativeUnitPartVoF", List.of("Äldreboende eller Hemtjänstgrupp"), "Del av förvaltning VoF"),
			tuple("unitVoF", List.of("05682 Förskola Copernicus, Bryggaregränd 1"), "Enhet VoF"),
			tuple("administrativeUnitPartIaF", List.of("Arbete och försörjning", "Barn, unga och familj", "Missbruk och psykisk ohälsa", "Vuxenutbildningen"), "Del av förvaltning IaF"),
			tuple("administrativeUnitPartKsK", List.of("Drakfastigheter", "Servicecenter",  "KSK Avdelningar", "Övrigt"), "Del av förvaltning KsK"),
			tuple("otherUnitsKsK", List.of("Test"), "Övriga enheter KsK"),
			tuple("administrativeUnitPartSbK", List.of("Byggavdelningen", "Gatuavdelningen", "Markavdelningen"), "Del av förvaltning SbK"),
			tuple("administrativeUnitPartLmK", List.of("Lantmäterikontoret", "Lantmäterimyndigheten"), "Del av förvaltning LmK"),
			tuple("administrativeUnitPartMK", List.of("Hela förvaltningen"), "Del av förvaltning MK"),
			tuple("administrativeUnitPartOfK", List.of("Övrigt"), "Del av förvaltning OfK"),
			tuple("otherUnitsOfK", List.of("Test", "Test2"), "Övriga enheter OfK"),
			tuple("startDate", List.of("2024-10-21"), "Startdatum"));

		assertThat(errand.getStakeholders()).hasSize(3).
			extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null),
				tuple(ROLE_APPLICANT, "Kalle", "Anka",  List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab", null, null),
				tuple(ROLE_USER, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab",  "PRIVATE", partyId));

		assertThat(errand.getExternalTags()).hasSize(1).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6920")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		verify(partyClient).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}