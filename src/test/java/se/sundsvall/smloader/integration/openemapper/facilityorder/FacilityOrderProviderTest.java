package se.sundsvall.smloader.integration.openemapper.facilityorder;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.util.LabelsProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.lenient;
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
		when(labelsProvider.getLabels(namespace)).thenReturn(List.of(
			new Label().id(labelId_1).resourcePath("VOF").resourceName(resourceName).classification(classification).displayName(displayName)
				.labels(List.of(new Label().id(labelId_2).resourcePath("VOF/CAREGIVER_SUPPORT_CENTER").resourceName(resourceName).classification(classification).displayName(displayName)))));

		final var stringBytes = readOpenEFile("flow-instance-lokal-planering.xml");

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
			tuple("department", List.of("Vård och omsorgsförvaltningen"), "Verksamhet"),
			tuple("operation", List.of("VOF/CAREGIVER_SUPPORT_CENTER"), "Kategori"),
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

		verify(properties).getFamilyId();
		verify(properties).getPriority();
		verify(properties).getCategory();
		verify(properties).getType();
		verify(caseMetaDataRepository).findByFamilyId(familyId);
		verify(labelsProvider).getLabels(namespace);
		verifyNoMoreInteractions(properties, labelsProvider, caseMetaDataRepository);
	}

	private static Stream<Arguments> labelsArgumentProvider() {
		return Stream.of(
			Arguments.of("VOF/HOMECARE", null, List.of("VOF", "VOF/HOMECARE")),
			Arguments.of("IAF/ADULTEDUCATION", null, List.of("IAF", "IAF/ADULTEDUCATION")),
			Arguments.of("VOF/SUPPORT_AND_CARE", null, List.of("VOF", "VOF/SUPPORT_AND_CARE")),
			Arguments.of("VOF/SUPPORT_AND_CARE", "VOF/SUPPORT_AND_CARE/GROUP_HOME", List.of("VOF", "VOF/SUPPORT_AND_CARE", "VOF/SUPPORT_AND_CARE/GROUP_HOME")),
			Arguments.of("VOF/SUPPORT_AND_CARE", "VOF/SUPPORT_AND_CARE/SUPPORTED_HOUSING", List.of("VOF", "VOF/SUPPORT_AND_CARE", "VOF/SUPPORT_AND_CARE/SUPPORTED_HOUSING")),
			Arguments.of("VOF/SUPPORT_AND_CARE", "UNKNOWN", List.of("VOF", "VOF/SUPPORT_AND_CARE")),
			Arguments.of("UNKNOWN", null, List.of()),
			Arguments.of("UNKNOWN", "UNKNOWN", List.of()),
			Arguments.of(null, null, List.of("E_POSTARENDE", "E_POSTARENDE/UNCATEGORIZED")));
	}

	@ParameterizedTest
	@MethodSource("labelsArgumentProvider")
	void mapToErrandLabels(final String operation, final String operationSubtype, final List<String> expectedLabelIds) throws Exception {
		// Arrange
		final var namespace = "namespace";
		final var familyId = "250";

		when(properties.getPriority()).thenReturn("MEDIUM");
		when(properties.getFamilyId()).thenReturn(familyId);
		lenient().when(properties.getLabels()).thenReturn(List.of("E_POSTARENDE", "E_POSTARENDE/UNCATEGORIZED"));
		when(caseMetaDataRepository.findByFamilyId(familyId)).thenReturn(new CaseMetaDataEntity().withNamespace(namespace).withFamilyId(familyId));
		when(labelsProvider.getLabels(namespace)).thenReturn(createLabelStructure());

		final var stringBytes = createXml(operation, operationSubtype);

		// Act
		final var errand = provider.mapToErrand(stringBytes);

		// Assert
		assertThat(errand.getLabels()).extracting(ErrandLabel::getId).containsExactlyElementsOf(expectedLabelIds);
	}

	private static byte[] createXml(final String operation, final String operationSubtype) throws Exception {
		final var operationElement = operation == null ? "" : "<operation><Value>" + operation + "</Value></operation>";
		final var subtypeElement = operationSubtype == null ? "" : "<operationSubtype><Value>" + operationSubtype + "</Value></operationSubtype>";
		final var xml = new String(readOpenEFile("flow-instance-lokal-planering.xml"), StandardCharsets.UTF_8);

		return xml.replace(xml.substring(xml.indexOf("<operation>"), xml.indexOf("</operation>") + "</operation>".length()), operationElement + subtypeElement)
			.getBytes(StandardCharsets.UTF_8);
	}

	// Label id is set to resource path to make assertions readable
	private static List<Label> createLabelStructure() {
		return List.of(
			label("VOF", List.of(
				label("VOF/HOMECARE", List.of()),
				label("VOF/SUPPORT_AND_CARE", List.of(
					label("VOF/SUPPORT_AND_CARE/DAY_ACTIVITY_CENTER", List.of()),
					label("VOF/SUPPORT_AND_CARE/GROUP_HOME", List.of()),
					label("VOF/SUPPORT_AND_CARE/SUPPORTED_HOUSING", List.of()))))),
			label("IAF", List.of(
				label("IAF/ADULTEDUCATION", List.of()))),
			label("E_POSTARENDE", List.of(
				label("E_POSTARENDE/UNCATEGORIZED", List.of()))));
	}

	private static Label label(final String resourcePath, final List<Label> labels) {
		return new Label().id(resourcePath).resourcePath(resourcePath).labels(labels);
	}
}
