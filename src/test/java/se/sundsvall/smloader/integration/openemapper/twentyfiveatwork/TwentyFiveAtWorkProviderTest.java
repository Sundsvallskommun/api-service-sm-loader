package se.sundsvall.smloader.integration.openemapper.twentyfiveatwork;

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
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_TWENTY_FIVE_AT_WORK;

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
class TwentyFiveAtWorkProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@Mock
	private LabelsProvider labelsProvider;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepository;

	@InjectMocks
	private TwentyFiveAtWorkProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("110");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("110");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"flow-instance-25-pa-jobbet.xml", "flow-instance-25-pa-jobbet-annan-adress.xml"
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
		when(properties.getLabels()).thenReturn(labels);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		final var stringBytes = readOpenEFile(oepErrandFile);

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_TWENTY_FIVE_AT_WORK);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getParameters()).hasSize(2).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactly(
			tuple("originalStartDate", List.of("2022-11-01"), "Startdatum för anställning"),
			tuple("latestStartDate", List.of("2021-01-01"), "Startdatum för anställning hos tidigare huvudman"));

		if (oepErrandFile.contains("annan-adress")) {
			assertThat(errand.getStakeholders()).hasSize(2).extracting(
				Stakeholder::getRole,
				Stakeholder::getFirstName,
				Stakeholder::getLastName,
				Stakeholder::getContactChannels,
				Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType,
				Stakeholder::getExternalId,
				Stakeholder::getAddress,
				Stakeholder::getZipCode,
				Stakeholder::getCity,
				Stakeholder::getParameters).containsExactlyInAnyOrder(
					tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, "PRIVATE", partyId, null, null, null, List.of(new Parameter()
						.key("administrationName")
						.values(List.of("KSK AVD Digitalisering IT stab")))),
					tuple(ROLE_EMPLOYEE, "Kalle", "Anka", emptyList(), null, "PRIVATE", partyId, "Avkroken 1", "99999", "Fjärran", emptyList()));
		} else {
			assertThat(errand.getStakeholders()).hasSize(2).extracting(
				Stakeholder::getRole,
				Stakeholder::getFirstName,
				Stakeholder::getLastName,
				Stakeholder::getContactChannels,
				Stakeholder::getOrganizationName,
				Stakeholder::getExternalIdType,
				Stakeholder::getExternalId,
				Stakeholder::getAddress,
				Stakeholder::getZipCode,
				Stakeholder::getCity,
				Stakeholder::getParameters).containsExactlyInAnyOrder(
					tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, "PRIVATE", partyId, null, null, null, List.of(new Parameter()
						.key("administrationName")
						.values(List.of("KSK AVD Digitalisering IT stab")))),
					tuple(ROLE_EMPLOYEE, "Kalle", "Anka", emptyList(), null, "PRIVATE", partyId, "Storgatan 1", "111 22", "ANKEBORG", emptyList()));
		}
		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("6857"),
			new ExternalTag().key("familyId").value("194")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		assertThat(errand.getLabels()).extracting(
			ErrandLabel::getResourcePath,
			ErrandLabel::getClassification,
			ErrandLabel::getResourceName,
			ErrandLabel::getDisplayName).containsExactly(tuple(label, classification, resourceName, displayName));

		verify(partyClient, times(2)).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
