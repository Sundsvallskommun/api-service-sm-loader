package se.sundsvall.smloader.integration.openemapper.substitutemanager;

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
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPROVER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_SUBSTITUTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_SUBSTITUTE_MANAGER;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.integration.util.LabelsProvider;

@ExtendWith(MockitoExtension.class)
class SubstituteManagerProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@Mock
	private LabelsProvider labelsProvider;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepository;

	@InjectMocks
	private SubstituteManagerProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("789");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("789");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"flow-instance-ersattare-chef.xml", "flow-instance-ersattare-chef-annan-skickar.xml"
	})
	void mapToErrand(String oepErrandFile) throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";
		final var label = "label";
		final var labels = List.of(label);
		final var namespace = "namespace";
		final var familyId = "789";
		final var resourceName = "resourceName";
		final var classification = "classification";
		final var displayName = "displayName";
		final var caseMetaDataEntity = new CaseMetaDataEntity().withNamespace(namespace).withFamilyId(familyId);

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(properties.getFamilyId()).thenReturn(familyId);
		when(caseMetaDataRepository.findByFamilyId(familyId)).thenReturn(caseMetaDataEntity);
		when(properties.getLabels()).thenReturn(labels);
		when(labelsProvider.getLabel(namespace, label)).thenReturn(new Label().resourcePath(label).resourceName(resourceName).classification(classification).displayName(displayName));
		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		final var stringBytes = readOpenEFile(oepErrandFile);

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_SUBSTITUTE_MANAGER);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(3).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactly(
			tuple("responsibilityNumber", List.of("25610000 - AoF Arenor i samverkan"), "Ordinarie chef ansvarsnummer"),
			tuple("startDate", List.of("2024-08-30"), "Attesteringsperiods startdatum"),
			tuple("endDate", List.of("2024-09-27"), "Attesteringsperiods slutdatum"));

		assertThat(errand.getLabels()).extracting(
			ErrandLabel::getResourcePath,
			ErrandLabel::getClassification,
			ErrandLabel::getResourceName,
			ErrandLabel::getDisplayName).containsExactly(tuple(label, classification, resourceName, displayName));

		assertThat(errand.getStakeholders()).hasSize(4).extracting(
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
				tuple(ROLE_MANAGER, "Kalle", "Anka", emptyList(), null, "PRIVATE", partyId, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK AVD Digitalisering IT stab")))),
				tuple(ROLE_SUBSTITUTE, "Tjatte", "Anka", emptyList(), null, "PRIVATE", partyId, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK AVD Digitalisering IT stab")))),
				tuple(ROLE_APPROVER, "Joakim", "von Anka", emptyList(), null, "PRIVATE", partyId, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK Avd Kansli och Säkerhet")))));
		assertThat(errand.getExternalTags()).containsExactlyInAnyOrder(
			new ExternalTag().key("caseId").value("6849"),
			new ExternalTag().key("familyId").value("182"));
		assertThat(errand.getReporterUserId()).isEqualTo("kall22ank");

		verify(partyClient, times(3)).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
