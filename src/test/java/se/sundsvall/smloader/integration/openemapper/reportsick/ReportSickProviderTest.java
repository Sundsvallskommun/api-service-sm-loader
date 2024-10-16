package se.sundsvall.smloader.integration.openemapper.reportsick;

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

import static java.util.Collections.emptyList;
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
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_REPORT_SICK;

@ExtendWith(MockitoExtension.class)
class ReportSickProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@InjectMocks
	private ReportSickProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("123");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("123");
	}

	@Test
	void mapToErrand() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";
		final var labels = List.of("label");

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(properties.getLabels()).thenReturn(labels);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		final var stringBytes = readOpenEFile("flow-instance-anmal-franvaro.xml");

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_REPORT_SICK);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getLabels()).isEqualTo(labels);
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(14).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactlyInAnyOrder(
			tuple("administrativeUnit", List.of("bou"), "Förvaltning/verksamhet"),
			tuple("employmentType", List.of("Månadsavlönad"), "Anställningsform"),
			tuple("employeeTitle", List.of("F040 Förskoleresurs"), "Befattning"),
			tuple("absentType", List.of("Ny sjukfrånvaro"), "Ny eller förlängd sjukfrånvaro"),
			tuple("absentStartDate", List.of("2024-10-11"), "Datum när sjukfrånvaro startade"),
			tuple("absentFirstDay", List.of("2024-07-31"), "Första sjukdagen"),
			tuple("absentDescription", List.of("Medarbetaren kom på arbetet och gick hem pga. sjukdom"), "Beskrivning av sjukfrånvaro"),
			tuple("absentStartTime", List.of("10:00:00"), "Tidpunkt för sjukfrånvarons start"),
			tuple("absentContinuation", List.of("Ja på heltid"), "Förlängning av sjukfrånvaro"),
			tuple("absentPeriodStartDate", List.of("2024-10-11"), "Sjukperiodens startdatum"),
			tuple("absentPeriodEndDate", List.of("2024-10-18"), "Sjukperiodens slutdatum"),
			tuple("sickNotePercentages", List.of("75", "100", "50", "25"), "Sjukskrivningsgrad i procent"),
			tuple("sickNoteStartDates", List.of("2024-07-01", "2024-07-01", "2024-07-01", "2024-07-04"), "Sjukskrivnings startdatum"),
			tuple("sickNoteEndDates", List.of("2024-07-01", "2024-07-01", "2024-07-01", "2024-07-04"), "Sjukskrivnings slutdatum"));

		assertThat(errand.getStakeholders()).hasSize(3).
			extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null),
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se"),
					new ContactChannel().type("Phone").value("0701112223")), "KSK Avd Digital Utveckling", null, null),
				tuple(ROLE_EMPLOYEE, "Kalle", "Anka", emptyList(), "KSK Avd Digital Utveckling", "PRIVATE", partyId));

		assertThat(errand.getExternalTags()).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6846")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		verify(partyClient).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}

	@Test
	void mapToErrandHourlyEmployed() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";
		final var labels = List.of("label");

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(properties.getLabels()).thenReturn(labels);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		final var stringBytes = readOpenEFile("flow-instance-anmal-franvaro-timanstalld.xml");

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_REPORT_SICK);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getLabels()).hasSize(1).isEqualTo(labels);
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(11).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactlyInAnyOrder(
			tuple("administrativeUnit", List.of("ks"), "Förvaltning/verksamhet"),
			tuple("employmentType", List.of("Timavlönad"), "Anställningsform"),
			tuple("absentStartDate", List.of("2024-10-11"), "Datum när sjukfrånvaro startade"),
			tuple("employeeTitle", List.of("3680 Barnskötare"), "Befattning"),
			tuple("absentDescription", List.of("Medarbetaren började senare pga. sjukdom"), "Beskrivning av sjukfrånvaro"),
			tuple("absentContinuation", List.of("Ja på deltid"), "Förlängning av sjukfrånvaro"),
			tuple("absentLateStartTime", List.of("14:00:00"), "Tidpunkt för senare start på grund av sjukdom"),
			tuple("sickPeriodDates", List.of("2024-10-11", "2024-10-14"), "Sjukperiodens datum"),
			tuple("sickPeriodStartTimes", List.of("14:00", "10:00"), "Sjukperiodens starttider"),
			tuple("sickPeriodEndTimes", List.of("17:00",	"14:00"), "Sjukperiodens sluttider"),
			tuple("haveSickNote", List.of("Nej"), "Har läkarintyg"));

		assertThat(errand.getStakeholders()).hasSize(3).
			extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType, Stakeholder::getExternalId).containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null),
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), "KSK AVD Digitalisering IT stab", null, null),
				tuple(ROLE_EMPLOYEE, "Kalle", "Anka", emptyList(), "KSK AVD Digitalisering IT stab",  "PRIVATE", partyId));

		assertThat(errand.getExternalTags()).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6910")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		verify(partyClient).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
