package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

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

		var stringBytes = readOpenEFile("flow-instance-foretradesratt-ateranstallning.xml");

		// Act
		var errand = mapper.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(3).extracting(Parameter::getKey, Parameter::getValues).containsExactlyInAnyOrder(
			tuple("startDate", List.of("2024-06-11")),
			tuple("workplace", List.of("Testplats")),
			tuple("position", List.of("6 - Arbetsmarknadsåtgärd")));

		assertThat(errand.getStakeholders()).hasSize(3).
			extracting(Stakeholder::getRole, Stakeholder::getFirstName, Stakeholder::getLastName, Stakeholder::getContactChannels, Stakeholder::getOrganizationName)
			.containsExactlyInAnyOrder(
				tuple(ROLE_CONTACT_PERSON, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null),
				tuple(ROLE_APPLICANT, null, null, List.of(new ContactChannel().type("Email").value("privat@test.se"),
					new ContactChannel().type("Phone").value("0701234567")), null),
				tuple(ROLE_MANAGER, "Joakim", "von Anka", List.of(new ContactChannel().type("Email").value("joakim.anka@sundsvall.se")), "KSK Avd Digital Utveckling"));

		assertThat(errand.getExternalTags()).containsExactlyElementsOf(List.of(new ExternalTag().key("caseId").value("6842")));

		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(properties);
	}
}
