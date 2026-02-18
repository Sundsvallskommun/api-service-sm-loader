package se.sundsvall.smloader.integration.openemapper.contactsalaryandpension;

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
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_USER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;

@ExtendWith(MockitoExtension.class)
class ContactSalaryAndPensionProviderTest {

	@Mock
	private PartyClient partyClient;

	@Mock
	private OpenEMapperProperties properties;

	@Mock
	private LabelsProvider labelsProvider;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepository;

	@InjectMocks
	private ContactSalaryAndPensionProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("789");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("789");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"flow-instance-kontakt-lon-pension.xml", "flow-instance-kontakt-lon-pension-no-subject.xml"
	})
	void mapToErrand(String testfile) throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";
		final var labels = List.of(category, type);
		final var namespace = "namespace";
		final var familyId = "789";
		final var labelId_1 = "labelId_1";
		final var labelId_2 = "labelId_2";
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
		when(labelsProvider.getLabels(namespace)).thenReturn(List.of(
			new Label().id(labelId_1).resourcePath(category).resourceName(resourceName).classification(classification).displayName(displayName)
				.labels(List.of(new Label().id(labelId_2).resourcePath(type).resourceName(resourceName).classification(classification).displayName(displayName)))));
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		final var stringBytes = readOpenEFile(testfile);

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		final var expectedTitle = testfile.equals("flow-instance-kontakt-lon-pension-no-subject.xml") ? "Kontakt lön och pension" : "Testar";
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(expectedTitle);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getDescription()).isEqualTo("Jag testar att skapa ett ärende.");

		assertThat(errand.getStakeholders()).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType,
			Stakeholder::getExternalId,
			Stakeholder::getParameters).containsExactly(
				tuple(ROLE_APPLICANT, "Kalle", "Anka", List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se")), null, "PRIVATE", partyId, List.of(new Parameter()
					.key("administrationName")
					.values(List.of("KSK AVD Digitalisering IT stab")))));

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("6854"),
			new ExternalTag().key("familyId").value("174")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		assertThat(errand.getLabels()).extracting(ErrandLabel::getId).containsExactly(labelId_1, labelId_2);

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
		final var subType = "subType";
		final var partyId = "partyId";
		final var labels = List.of(category, type);
		final var namespace = "namespace";
		final var familyId = "789";
		final var labelId_1 = "labelId_1";
		final var labelId_2 = "labelId_2";
		final var labelId_3 = "labelId_3";
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
		when(labelsProvider.getLabels(namespace)).thenReturn(List.of(
			new Label().id(labelId_1).resourcePath(category).resourceName(resourceName).classification(classification).displayName(displayName)
				.labels(List.of(new Label().id(labelId_2).resourcePath(type).resourceName(resourceName).classification(classification).displayName(displayName)
					.labels(List.of(new Label().id(labelId_3).resourcePath(subType).resourceName(resourceName).classification(classification).displayName(displayName)))))));

		when(properties.getPriority()).thenReturn(priority);
		when(properties.getCategory()).thenReturn(category);
		when(properties.getType()).thenReturn(type);
		when(partyClient.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

		final var stringBytes = readOpenEFile("flow-instance-kontakt-lon-pension-chef.xml");

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();
		assertThat(errand.getDescription()).isEqualTo("Jag testar som chef eller HR-administratör");

		assertThat(errand.getStakeholders()).hasSize(3).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getExternalIdType,
			Stakeholder::getExternalId,
			Stakeholder::getParameters).containsExactlyInAnyOrder(
				tuple(ROLE_MANAGER, "Kalle", "Anka", emptyList(), null, "PRIVATE", partyId, List.of(new Parameter().key("administrationName").values(List.of("KSK AVD Digitalisering IT stab")))),
				tuple(ROLE_USER, "Knatte", "Anka", List.of(new ContactChannel().type("Email").value("knatte.anka@sundsvall.se")), null, "PRIVATE", partyId, emptyList()),
				tuple(ROLE_USER, "Tjatte", "Anka", List.of(new ContactChannel().type("Email").value("tjatte.anka@sundsvall.se")), null, "PRIVATE", partyId, emptyList()));

		assertThat(errand.getLabels()).extracting(ErrandLabel::getId).containsExactly(labelId_1, labelId_2);

		assertThat(errand.getReporterUserId()).isEqualTo("chefAnvändare");
		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("6873"),
			new ExternalTag().key("familyId").value("174")));

		verify(partyClient, times(3)).getPartyId(anyString(), any(), anyString());
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(partyClient, properties);
	}
}
