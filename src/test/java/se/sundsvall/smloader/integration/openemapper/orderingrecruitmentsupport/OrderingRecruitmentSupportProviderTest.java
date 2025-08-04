package se.sundsvall.smloader.integration.openemapper.orderingrecruitmentsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_RECRUITING_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;

@ExtendWith(MockitoExtension.class)
class OrderingRecruitmentSupportProviderTest {

	@Mock
	private OpenEMapperProperties properties;

	@InjectMocks
	private OrderingRecruitmentSupportProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("123");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("123");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"flow-instance-rekryteringsstod.xml", "flow-instance-rekryteringsstod-tidsbegransad.xml"
	})
	void mapToErrand(String fileName) throws Exception {

		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);

		final var stringBytes = readOpenEFile(fileName);

		final var errand = provider.mapToErrand(stringBytes);

		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo("Spion");
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category("PARTIAL_PACKAGE").type("PARTIAL_PACKAGE.EMPLOYEE"));
		assertThat(errand.getLabels()).isEqualTo(List.of("PARTIAL_PACKAGE", "PARTIAL_PACKAGE.EMPLOYEE"));
		assertThat(errand.getBusinessRelated()).isFalse();
		if (fileName.equals("flow-instance-rekryteringsstod.xml")) {
			assertThat(errand.getParameters()).hasSize(33).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName, Parameter::getGroup).containsExactly(
				tuple("municipalityOrCompany", List.of("Kommunal förvaltning"), "Gäller kommunal förvaltning eller bolag och förbund?", null),
				tuple("department", List.of("Kommunstyrelsekontoret"), "Organisation", null),
				tuple("referenceNumber", List.of("007007007 - Bond James"), "Referensnummer för mottagare av fakturan", null),
				tuple("securityClassification", List.of("Ja"), "Behöver tjänsten säkerhetsklassas?", null),
				tuple("orderDetails", List.of("Delpaket"), "Beställning", null),
				tuple("position", List.of("Medarbetare"), "Tjänst", null),
				tuple("readyProfile", List.of("Jag har en färdig annons"), "Färdig annons eller stöd i annonsutformning", null),
				tuple("workplaceTitle", List.of("Spion"), "Tjänstetitel", null),
				tuple("workplace", List.of("MI6 Headquarters"), "Arbetsplats", null),
				tuple("employmentType", List.of("Tillsvidareanställning"), "Anställningsform", null),
				tuple("startDate", List.of("2025-04-24"), "Tillträde", null),
				tuple("scope", List.of("100"), "Omfattning i procent", null),
				tuple("numberOfPositions", List.of("1"), "Antal tjänster", null),
				tuple("recruitmentAccess", List.of("Miss Moneypenny|miss.moneypenny@mi6.se|Sekreterare"), "Namn|E-Post|Roll", null),
				tuple("workplaceInfo", List.of("Arbete över hela världen"), "Om arbetsplatsen", null),
				tuple("jobTask", List.of("Insamling av information"), "Arbetsuppgifter", null),
				tuple("qualifications", List.of("Stor och stark"), "Kvalifikationer", null),
				tuple("advertising", List.of("Intern och extern"), "Annonsering", null),
				tuple("advertisementContacts", List.of("M|Chef|060-666666"), "Namn|Title|Telefonnummer", null),
				tuple("unionContacts", List.of("Ulla Ullasson|städförbundet|010-1234567"), "Namn|Fackförbund|Telefonnummer", null),
				tuple("addSelectionQuestions", List.of("Ja"), "Vill du lägga till urvalsfrågor? (timdebitering)", null),
				tuple("question1", List.of("Hur långt är ett snöre?"), "Fråga 1", null),
				tuple("question2", List.of("Vad är klockan?"), "Fråga 2", null),
				tuple("question3", List.of("Finns det tårta?"), "Fråga 3", null),
				tuple("advertisementType", List.of("Intern och extern"), "Annonsering", null),
				tuple("advertisementTime", List.of("2 Veckor"), "Annonseringtid", null),
				tuple("additionalSupport", List.of("Utökad annonsering (sociala medier)", "Tester"), "Önskar stöd i rekrytering", null),
				tuple("mediaChoices", List.of("Målgruppsstyrda annonser (sociala medier)"), "Mediaval", null),
				tuple("advertisementPackage", List.of("Large"), "Annonspaket", null),
				tuple("testType", List.of("Personlighetstest"), "Beställda tester", null),
				tuple("inDepthInterview", List.of("Ja"), "Fördjupad intervju med kandidater beställd", null),
				tuple("recruitmentSupport", List.of("Okej"), "Konsultativt rekryteringsstöd per timme", null),
				tuple("additionalInformation", List.of("Lycka till!"), "Övrig information", null));
		} else {
			assertThat(errand.getParameters()).hasSize(34).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName, Parameter::getGroup).containsExactly(
				tuple("municipalityOrCompany", List.of("Kommunal förvaltning"), "Gäller kommunal förvaltning eller bolag och förbund?", null),
				tuple("department", List.of("Kommunstyrelsekontoret"), "Organisation", null),
				tuple("referenceNumber", List.of("007007007 - Bond James"), "Referensnummer för mottagare av fakturan", null),
				tuple("securityClassification", List.of("Ja"), "Behöver tjänsten säkerhetsklassas?", null),
				tuple("orderDetails", List.of("Delpaket"), "Beställning", null),
				tuple("position", List.of("Medarbetare"), "Tjänst", null),
				tuple("readyProfile", List.of("Jag har en färdig annons"), "Färdig annons eller stöd i annonsutformning", null),
				tuple("workplaceTitle", List.of("Spion"), "Tjänstetitel", null),
				tuple("workplace", List.of("MI6 Headquarters"), "Arbetsplats", null),
				tuple("employmentType", List.of("Tidsbegränsad anställning"), "Anställningsform", null),
				tuple("startDate", List.of("2025-06-11"), "Tillträde", null),
				tuple("endDate", List.of("2025-06-12"), "Slutdatum", null),
				tuple("scope", List.of("100"), "Omfattning i procent", null),
				tuple("numberOfPositions", List.of("1"), "Antal tjänster", null),
				tuple("recruitmentAccess", List.of("Miss Moneypenny|miss.moneypenny@mi6.se|Sekreterare"), "Namn|E-Post|Roll", null),
				tuple("workplaceInfo", List.of("Arbete över hela världen"), "Om arbetsplatsen", null),
				tuple("jobTask", List.of("Insamling av information"), "Arbetsuppgifter", null),
				tuple("qualifications", List.of("Stor och stark"), "Kvalifikationer", null),
				tuple("advertising", List.of("Intern och extern"), "Annonsering", null),
				tuple("advertisementContacts", List.of("M|Chef|060-666666"), "Namn|Title|Telefonnummer", null),
				tuple("unionContacts", List.of("Ulla Ullasson|städförbundet|010-1234567"), "Namn|Fackförbund|Telefonnummer", null),
				tuple("addSelectionQuestions", List.of("Ja"), "Vill du lägga till urvalsfrågor? (timdebitering)", null),
				tuple("question1", List.of("Hur långt är ett snöre?"), "Fråga 1", null),
				tuple("question2", List.of("Vad är klockan?"), "Fråga 2", null),
				tuple("question3", List.of("Finns det tårta?"), "Fråga 3", null),
				tuple("advertisementType", List.of("Intern och extern"), "Annonsering", null),
				tuple("advertisementTime", List.of("2 Veckor"), "Annonseringtid", null),
				tuple("additionalSupport", List.of("Utökad annonsering (sociala medier)", "Tester"), "Önskar stöd i rekrytering", null),
				tuple("mediaChoices", List.of("Målgruppsstyrda annonser (sociala medier)"), "Mediaval", null),
				tuple("advertisementPackage", List.of("Large"), "Annonspaket", null),
				tuple("testType", List.of("Personlighetstest"), "Beställda tester", null),
				tuple("inDepthInterview", List.of("Ja"), "Fördjupad intervju med kandidater beställd", null),
				tuple("recruitmentSupport", List.of("Okej"), "Konsultativt rekryteringsstöd per timme", null),
				tuple("additionalInformation", List.of("Lycka till!"), "Övrig information", null));
		}
		assertThat(errand.getStakeholders()).hasSize(2).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType,
			Stakeholder::getExternalId,
			Stakeholder::getParameters).containsExactlyInAnyOrder(
				tuple(ROLE_APPLICANT,
					"James",
					"Bond",
					List.of(new ContactChannel().type("Email").value("james.bond@mi6.se"), new ContactChannel().type("Phone").value("060-007007")),
					null,
					null,
					null,
					List.of(
						new Parameter().key("administrationName").values(List.of("MI6")).displayName("Organisation"),
						new Parameter().key("TITLE").values(List.of("Spion")).displayName("Tjänstetitel"),
						new Parameter().key("userId").values(List.of("007")).displayName("Användar-id"))),
				tuple(ROLE_RECRUITING_MANAGER,
					"M",
					"Unknown",
					List.of(new ContactChannel().type("Email").value("m.unknown@mi6.se"), new ContactChannel().type("Phone").value("060-000000")),
					null,
					null,
					null,
					List.of(new Parameter().key("TITLE").values(List.of("Chief of Secret Intelligence Service")).displayName("Tjänstetitel"))));
		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(
			new ExternalTag().key("caseId").value("7287"),
			new ExternalTag().key("familyId").value("224")));

		assertThat(errand.getReporterUserId()).isEqualTo("007");
	}

	@ParameterizedTest
	@CsvSource(value = {
		"Chef:COMPLETE_RECRUITMENT.MANAGER",
		"Medarbetare:COMPLETE_RECRUITMENT.EMPLOYEE",
		"Volymrekrytering:COMPLETE_RECRUITMENT.VOLUME",
		"Omtag:COMPLETE_RECRUITMENT.RETAKE"
	}, delimiter = ':')
	void mapWithDifferentClassifications(String input, String expected) {
		when(properties.getPriority()).thenReturn("MEDIUM");

		var xml = """
			<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
			<FlowInstance xmlns="http://www.oeplatform.org/version/2.0/schemas/flowinstance" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.oeplatform.org/version/2.0/schemas/flowinstance schema-551.xsd">
				<Values>
					<orderDetails>
			  			<Value>Fullständig rekryteringsprocess</Value>
			  		</orderDetails>
					<position>
			  			<Value>%s</Value>
			  		</position>
				</Values>
			</FlowInstance>
			""".formatted(input);

		final var errand = provider.mapToErrand(xml.getBytes(StandardCharsets.ISO_8859_1));

		assertThat(errand.getClassification().getCategory()).isEqualTo("COMPLETE_RECRUITMENT");
		assertThat(errand.getClassification().getType()).isEqualTo(expected);
	}

	@Test
	void throwExceptionWhenUnsupportedPosition() {
		when(properties.getPriority()).thenReturn("MEDIUM");

		final var xml = """
			<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
			<FlowInstance xmlns="http://www.oeplatform.org/version/2.0/schemas/flowinstance" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.oeplatform.org/version/2.0/schemas/flowinstance schema-551.xsd">
				<Values>
					<orderDetails>
			  			<Value>Fullständig rekryteringsprocess</Value>
			  		</orderDetails>
					<position>
			  			<Value>Unsupported Position</Value>
			  		</position>
				</Values>
			</FlowInstance>
			""";
		final var bytes = xml.getBytes(StandardCharsets.ISO_8859_1);
		final var exception = assertThrows(ThrowableProblem.class, () -> provider.mapToErrand(bytes));

		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(exception.getDetail()).isEqualTo("Unsupported recruitment position: Unsupported Position");
	}
}
