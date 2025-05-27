package se.sundsvall.smloader.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

	@Mock
	private SupportManagementClient mockSupportManagementClient;

	@Mock
	private Page<Errand> pageMock;

	@Captor
	private ArgumentCaptor<List<Parameter>> parametersCaptor;

	private MigrationService migrationService;

	@Test
	void migrateReportSickErrands() {
		// Arrange
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";

		migrationService = new MigrationService(mockSupportManagementClient);

		final var errandWithSickPeriods = new Errand()
			.id("1")
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.externalTags(Set.of(new ExternalTag().key("familyId").value(familyId), new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE_INTERNAL")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))))
			.activeNotifications(emptyList())
			.parameters(List.of(new Parameter().key("sickPeriodDates").values(List.of("2025-01-02", "2025-01-10", "2025-02-01"))
				.displayName("Sjukskrivnings startdatum")
				.group("sickPeriod"),
				new Parameter().key("sickPeriodStartTimes").values(List.of("14:00", "10:00", "12:00"))
					.displayName("Sjukperiodens starttider")
					.group("sickPeriod"),
				new Parameter().key("sickPeriodEndTimes").values(List.of("17:00", "14:00", "19:00"))
					.displayName("Sjukperiodens sluttider")
					.group("sickPeriod"),
				new Parameter("test").values(emptyList())));
		final var errandWithSickNotes = new Errand()
			.id("2")
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.externalTags(Set.of(new ExternalTag().key("familyId").value(familyId), new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE_INTERNAL")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))))
			.activeNotifications(emptyList())
			.parameters(List.of(new Parameter().key("sickNotePercentages").values(List.of("75", "100", "50"))
				.displayName("Sjukskrivningsgrad i procent")
				.group("sickNote"),
				new Parameter().key("sickNoteStartDates").values(List.of("2025-05-02", "2025-05-03", "2025-05-10"))
					.displayName("Sjukperiodens starttider")
					.group("sickNote"),
				new Parameter().key("sickNoteEndDates").values(List.of("2025-05-03", "2025-05-04", "2025-05-20"))
					.displayName("Sjukperiodens sluttider")
					.group("sickNote"),
				new Parameter().key("timeCare").values(List.of("Ja", "", "Nej"))
					.displayName("Sjukperiodens sluttider")
					.group("sickNote"),
				new Parameter().key("currentSchedule").values(List.of("Ja", "Nej", ""))
					.displayName("Sjukperiodens sluttider")
					.group("sickNote"),
				new Parameter("test").values(emptyList())));

		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(pageMock);
		when(pageMock.getContent()).thenReturn(List.of(errandWithSickNotes, errandWithSickPeriods));

		// Act
		migrationService.migrateReportSick(namespace, municipalityId);

		// Assert and verify
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(parameters.key:'sickNoteStartDates' or parameters.key:'sickPeriodDates') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient, times(2)).updateErrandParameters(eq(municipalityId), eq(namespace), anyString(), parametersCaptor.capture());

		final var capturedParameters = parametersCaptor.getAllValues();

		assertThat(capturedParameters).hasSize(2);
		assertThat(capturedParameters.getFirst()).hasSize(2).extracting(
			Parameter::getKey,
			Parameter::getValues,
			Parameter::getDisplayName,
			Parameter::getGroup)
			.containsExactlyInAnyOrder(
				tuple("sickNotes", List.of("2025-05-02|2025-05-03|75|Ja|Ja", "2025-05-03|2025-05-04|100||Nej", "2025-05-10|2025-05-20|50|Nej|"),
					"Startdatum|Slutdatum|Sjukskrivningsgrad i procent|Använder TimeCare|Pågående schemaperiod", "Sjukskrivning"),
				tuple("test", emptyList(), null, null));
		assertThat(capturedParameters.getLast()).hasSize(2).extracting(
			Parameter::getKey,
			Parameter::getValues,
			Parameter::getDisplayName,
			Parameter::getGroup)
			.containsExactlyInAnyOrder(
				tuple("sickPeriods", List.of("2025-01-02|14:00|17:00", "2025-01-10|10:00|14:00", "2025-02-01|12:00|19:00"), "Datum|Starttid|Sluttid", "Sjukperioder"),
				tuple("test", emptyList(), null, null));
	}

	@Test
	void migrateReportSickErrandsMissingTimeCareAndCurrentSchedule() {
		// Arrange
		final var familyId = "161";
		final var flowInstanceId = "123456";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";

		migrationService = new MigrationService(mockSupportManagementClient);

		final var errandWithSickNotes = new Errand()
			.id("1")
			.classification(new Classification()
				.category("category")
				.type("type"))
			.description("description")
			.externalTags(Set.of(new ExternalTag().key("familyId").value(familyId), new ExternalTag().key("caseId").value(flowInstanceId)))
			.channel("ESERVICE_INTERNAL")
			.stakeholders(List.of(new Stakeholder()
				.role("role")
				.firstName("firstName")
				.lastName("lastName")
				.contactChannels(List.of(new ContactChannel()
					.type("email")
					.value("a.b@c")))))
			.activeNotifications(emptyList())
			.parameters(List.of(new Parameter().key("sickNotePercentages").values(List.of("75", "100", "50"))
				.displayName("Sjukskrivningsgrad i procent")
				.group("sickNote"),
				new Parameter().key("sickNoteStartDates").values(List.of("2025-05-02", "2025-05-03", "2025-05-10"))
					.displayName("Sjukperiodens starttider")
					.group("sickNote"),
				new Parameter().key("sickNoteEndDates").values(List.of("2025-05-03", "2025-05-04", "2025-05-20"))
					.displayName("Sjukperiodens sluttider")
					.group("sickNote")));

		when(mockSupportManagementClient.findErrands(any(), any(), any())).thenReturn(pageMock);
		when(pageMock.getContent()).thenReturn(List.of(errandWithSickNotes));

		// Act
		migrationService.migrateReportSick(namespace, municipalityId);

		// Assert and verify
		verify(mockSupportManagementClient).findErrands(municipalityId, namespace, "exists(parameters.key:'sickNoteStartDates' or parameters.key:'sickPeriodDates') and channel:'ESERVICE_INTERNAL'");
		verify(mockSupportManagementClient).updateErrandParameters(eq(municipalityId), eq(namespace), anyString(), parametersCaptor.capture());

		final var capturedParameters = parametersCaptor.getAllValues();

		assertThat(capturedParameters).hasSize(1);
		assertThat(capturedParameters.getFirst()).hasSize(1).extracting(
			Parameter::getKey,
			Parameter::getValues,
			Parameter::getDisplayName,
			Parameter::getGroup)
			.containsExactlyInAnyOrder(
				tuple("sickNotes", List.of("2025-05-02|2025-05-03|75||", "2025-05-03|2025-05-04|100||", "2025-05-10|2025-05-20|50||"),
					"Startdatum|Slutdatum|Sjukskrivningsgrad i procent|Använder TimeCare|Pågående schemaperiod", "Sjukskrivning"));
	}
}
