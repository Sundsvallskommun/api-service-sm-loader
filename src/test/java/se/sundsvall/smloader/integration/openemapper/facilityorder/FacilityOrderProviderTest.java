package se.sundsvall.smloader.integration.openemapper.facilityorder;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.util.LabelsProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_FACILITY_ORDER;

@ExtendWith(MockitoExtension.class)
class FacilityOrderProviderTest {

	@Mock
	private OpenEMapperProperties properties;

	@Mock
	private LabelsProvider labelsProvider;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepository;

	@InjectMocks
	private FacilityOrderProvider provider;

	@Test
	void getSupportedFamilyId() {
		when(properties.getFamilyId()).thenReturn("250");

		assertThat(provider.getSupportedFamilyId()).isEqualTo("250");
	}

	@Test
	void mapToErrand() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var labels = List.of(category, type);
		final var namespace = "namespace";
		final var familyId = "250";
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

		final var stringBytes = readOpenEFile("flow-instance-lokaler-vof-iaf.xml");

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert and verify
		assertThat(errand.getStatus()).isEqualTo(STATUS_NEW);
		assertThat(errand.getTitle()).isEqualTo(TITLE_FACILITY_ORDER);
		assertThat(errand.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(errand.getChannel()).isEqualTo(INTERNAL_CHANNEL_E_SERVICE);
		assertThat(errand.getClassification()).isEqualTo(new Classification().category(category).type(type));
		assertThat(errand.getBusinessRelated()).isFalse();

		assertThat(errand.getParameters()).extracting(Parameter::getKey, Parameter::getValues, Parameter::getDisplayName).containsExactly(
			tuple("administration", List.of("Vård och omsorgsförvaltningen"), "Förvaltning"),
			tuple("operation", List.of("Anhörigcentrum"), "Verksamhet"),
			tuple("orderDetails", List.of("Utökning av befintlig lokal"), "Beställning"),
			tuple("property", List.of("VoF Test Fastigheten 1"), "Fastighet"),
			tuple("workplaceUnit", List.of("Avdelning Ängen"), "Avdelning"),
			tuple("room", List.of("Lägenhet 4"), "Specifikt rum"),
			tuple("operationInformation", List.of("Behov ett och behov två"), "Beskrivning av behov"));

		assertThat(errand.getStakeholders()).extracting(
			Stakeholder::getRole,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getContactChannels,
			Stakeholder::getParameters).containsExactly(
				tuple(ROLE_APPLICANT, "Kalle", "Anka",
					List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se"),
						new ContactChannel().type("Phone").value("0701234567"),
						new ContactChannel().type("Phone").value("0709876543")),
					List.of(new Parameter().key("administrationName").values(List.of("KSK AVD Digitalisering IT stab")).displayName("Organisation"),
						new Parameter().key("title").values(List.of("Systemutvecklare")).displayName("Tjänstetitel"),
						new Parameter().key("userId").values(List.of("kal00ank")).displayName("Användar-id"))));

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrderElementsOf(List.of(new ExternalTag().key("caseId").value("7552"),
			new ExternalTag().key("familyId").value("250")));
		assertThat(errand.getReporterUserId()).isEqualTo("kal00ank");

		assertThat(errand.getLabels()).extracting(ErrandLabel::getId).containsExactly(labelId_1, labelId_2);

		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verifyNoMoreInteractions(properties);
	}
}
