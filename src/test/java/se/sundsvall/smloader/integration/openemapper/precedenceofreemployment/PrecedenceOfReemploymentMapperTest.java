package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PRECEDENCE_OF_REEMPLOYMENT;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;

@ExtendWith(MockitoExtension.class)
class PrecedenceOfReemploymentMapperTest {

	@Mock
	private OpenEMapperProperties properties;

	@InjectMocks
	private PrecedenceOfReemploymentMapper mapper;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("123");

		assertThat(mapper.getSupportedFamilyId()).isEqualTo("123");
	}

	@Test
	void mapToErrand() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);

		final var stringBytes = readOpenEFile("flow-instance-foretradesratt-ateranstallning.xml");

		// Act
		final var errand = mapper.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_PRECEDENCE_OF_REEMPLOYMENT);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(EXTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(5).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactly(
			tuple("workplace", List.of("Testar"), "Arbetsplats"),
			tuple("position", List.of("T - Tidsbegr anställning"), "Löneform"),
			tuple("lastDayOfPosition", List.of("2024-10-03"), "Sista anställningsdag"),
			tuple("manager", List.of("Joakim von Anka"), "Chef"),
			tuple("salaryType", List.of("Månadslön"), "Löneform"));

		assertThat(errand.getStakeholders()).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getParameters)
			.containsExactly(
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se"), new ContactChannel().type("Phone").value("0701112223")), null, emptyList()));

		assertThat(errand.getLabels()).hasSize(2).containsExactlyElementsOf(List.of(category, type));
		assertThat(errand.getExternalTags()).containsExactlyInAnyOrder(
			new ExternalTag().key("caseId").value("6911"),
			new ExternalTag().key("familyId").value("197"));

		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");

		verify(properties).getPriority();
		verify(properties, times(2)).getCategory();
		verify(properties, times(2)).getType();
		verifyNoMoreInteractions(properties);
	}
}
