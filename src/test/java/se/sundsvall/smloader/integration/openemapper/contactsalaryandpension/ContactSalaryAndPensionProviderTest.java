package se.sundsvall.smloader.integration.openemapper.contactsalaryandpension;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
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

@ExtendWith(MockitoExtension.class)
class ContactSalaryAndPensionProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@InjectMocks
	private ContactSalaryAndPensionProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("789");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("789");
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

		var stringBytes = readOpenEFile("flow-instance-kontakt-lon-pension.xml");

		// Act
		var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo("Testar");
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getDescription()).isEqualTo("Jag testar att skapa ett ärende.");

		assertThat(errand.getStakeholders()).hasSize(2).extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab", "PRIVATE", partyId),
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null));

		assertThat(errand.getExternalTags()).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6854")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		verify(partyClient).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}

	@Test
	void mapToErrandWhenManager() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		var stringBytes = readOpenEFile("flow-instance-kontakt-lon-pension-chef.xml");

		// Act
		var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getDescription()).isEqualTo("Jag testar som chef eller HR-administratör");

		assertThat(errand.getStakeholders()).hasSize(4).extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null),
				tuple(ROLE_MANAGER, "Kalle", "Anka", emptyList(), "KSK AVD Digitalisering IT stab", "PRIVATE", partyId),
				tuple(ROLE_USER, "Knatte", "Anka", List.of(new ContactChannel().type("Email").value("knatte.anka@sundsvall.se")), null, "PRIVATE", partyId),
				tuple(ROLE_USER, "Tjatte", "Anka", List.of(new ContactChannel().type("Email").value("tjatte.anka@sundsvall.se")), null, "PRIVATE", partyId));

		assertThat(errand.getReporterUserId()).isEqualTo("chefAnvändare");
		assertThat(errand.getExternalTags()).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6873")));

		verify(partyClient, times(3)).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
