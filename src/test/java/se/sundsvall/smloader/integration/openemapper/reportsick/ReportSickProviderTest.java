package se.sundsvall.smloader.integration.openemapper.reportsick;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.GROUP_SICK_NOTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.GROUP_SICK_PERIOD;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_REPORT_SICK;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;

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
		assertThat(errand.getParameters()).hasSize(16).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName, Parameter::getGroup).containsExactly(
			tuple("administrativeUnit", List.of("bou"), "Förvaltning/verksamhet", null),
			tuple("employmentType", List.of("Månadsavlönad"), "Löneform", null),
			tuple("employeeTitle", List.of("F040 Förskoleresurs"), "Befattning", null),
			tuple("absentType", List.of("Ny sjukfrånvaro"), "Ny eller förlängd sjukfrånvaro", null),
			tuple("absentFirstDay", List.of("2024-07-31"), "Första sjukdagen", null),
			tuple("absentStartDate", List.of("2024-10-11"), "Datum när sjukfrånvaro startade", null),
			tuple("absentDescription", List.of("Medarbetaren kom på arbetet och gick hem pga. sjukdom"), "Beskrivning av sjukfrånvaro", null),
			tuple("absentStartTime", List.of("10:00:00"), "Tidpunkt för sjukfrånvarons start", null),
			tuple("absentContinuation", List.of("Ja på heltid"), "Förlängning av sjukfrånvaro", null),
			tuple("absentPeriodStartDate", List.of("2024-10-11"), "Sjukperiodens startdatum", null),
			tuple("absentPeriodEndDate", List.of("2024-10-18"), "Sjukperiodens slutdatum", null),
			tuple("sickNotePercentages", List.of("75", "100", "50", "25"), "Sjukskrivningsgrad i procent", GROUP_SICK_NOTE),
			tuple("sickNoteStartDates", List.of("2024-07-01", "2024-07-01", "2024-07-01", "2024-07-04"), "Sjukskrivnings startdatum", GROUP_SICK_NOTE),
			tuple("sickNoteEndDates", List.of("2024-07-31", "2024-07-31", "2024-07-31", "2024-07-18"), "Sjukskrivnings slutdatum", GROUP_SICK_NOTE),
			tuple("timeCare", List.of("Ja", "", "Nej", ""), "Använder verksamheten TimeCare?", GROUP_SICK_NOTE),
			tuple("currentSchedule", List.of("Ja", "", "Nej", ""), "Finns pågående schemaperiod?", GROUP_SICK_NOTE));

		assertThat(errand.getStakeholders()).hasSize(2).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType,
			Stakeholder::getExternalId,
			Stakeholder::getParameters).containsExactlyInAnyOrder(
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se"), new ContactChannel().type("Phone").value("0701112223")), null, null, null, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK Avd Digital Utveckling")))),
				tuple(ROLE_EMPLOYEE, "Kalle", "Anka", emptyList(), null, "PRIVATE", partyId, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK Avd Digital Utveckling")))));

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("6846"),
			new ExternalTag().key("familyId").value("195")));
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
		assertThat(errand.getParameters()).hasSize(11).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName, Parameter::getGroup).containsExactly(
			tuple("administrativeUnit", List.of("ks"), "Förvaltning/verksamhet", null),
			tuple("employmentType", List.of("Timavlönad"), "Löneform", null),
			tuple("employeeTitle", List.of("3680 Barnskötare"), "Befattning", null),
			tuple("absentStartDate", List.of("2024-10-11"), "Datum när sjukfrånvaro startade", null),
			tuple("absentDescription", List.of("Medarbetaren började senare pga. sjukdom"), "Beskrivning av sjukfrånvaro", null),
			tuple("absentLateStartTime", List.of("14:00:00"), "Tidpunkt för senare start på grund av sjukdom", null),
			tuple("absentContinuation", List.of("Ja på deltid"), "Förlängning av sjukfrånvaro", null),
			tuple("haveSickNote", List.of("Nej"), "Har läkarintyg", null),
			tuple("sickPeriodDates", List.of("2024-10-11", "2024-10-14"), "Sjukperiodens datum", GROUP_SICK_PERIOD),
			tuple("sickPeriodStartTimes", List.of("14:00", "10:00"), "Sjukperiodens starttider", GROUP_SICK_PERIOD),
			tuple("sickPeriodEndTimes", List.of("17:00", "14:00"), "Sjukperiodens sluttider", GROUP_SICK_PERIOD));

		assertThat(errand.getStakeholders()).hasSize(2).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType,
			Stakeholder::getExternalId,
			Stakeholder::getParameters).containsExactlyInAnyOrder(
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, null, null, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK AVD Digitalisering IT stab")))),
				tuple(ROLE_EMPLOYEE, "Kalle", "Anka", emptyList(), null, "PRIVATE", partyId, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK AVD Digitalisering IT stab")))));

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("6910"),
			new ExternalTag().key("familyId").value("195")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		verify(partyClient).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
