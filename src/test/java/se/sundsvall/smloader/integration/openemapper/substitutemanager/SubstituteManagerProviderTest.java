package se.sundsvall.smloader.integration.openemapper.substitutemanager;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.smloader.Application;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;

import java.util.List;
import java.util.Map;
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
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPROVER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_SUBSTITUTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class SubstituteManagerProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@InjectMocks
	private SubstituteManagerProvider provider;

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

		var stringBytes = readOpenEFile("flow-instance-ersattare-chef.xml");

		// Act
		var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(3).containsExactlyInAnyOrderEntriesOf(
			Map.of("startDate", List.of("2024-08-30"),
				"endDate", List.of("2024-09-27"),
				"responsibilityNumber", List.of("25610000 - AoF Arenor i samverkan"))
		);

		assertThat(errand.getStakeholders()).hasSize(4).
			extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null),
				tuple(ROLE_MANAGER, "Kalle", "Anka", emptyList(), "KSK AVD Digitalisering IT stab", "PRIVATE", partyId),
				tuple(ROLE_SUBSTITUTE, "Tjatte", "Anka", emptyList(), "KSK AVD Digitalisering IT stab",  "PRIVATE", partyId),
				tuple(ROLE_APPROVER, "Joakim", "von Anka", emptyList(), "KSK Avd Kansli och Säkerhet", "PRIVATE", partyId));
		assertThat(errand.getExternalTags()).hasSize(1).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6849")));

		verify(partyClient, times(3)).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
