package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.smloader.TestUtil.readOpenEFile;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PRECEDENCE_OF_REEMPLOYMENT;

import generated.se.sundsvall.party.PartyType;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.integration.util.LabelsProvider;

@ExtendWith(MockitoExtension.class)
class PrecedenceOfReemploymentProviderTest {

	@Mock
	private OpenEMapperProperties propertiesMock;

	@Mock
	private PartyClient partyClientMock;

	@Mock
	private LabelsProvider labelsProviderMock;

	@Mock
	private CaseMetaDataRepository caseMetaDataRepositoryMock;

	@InjectMocks
	private PrecedenceOfReemploymentProvider mapper;

	@Test
	void getSupportedFamilyId() {
		when(propertiesMock.getFamilyId()).thenReturn("123");

		assertThat(mapper.getSupportedFamilyId()).isEqualTo("123");
	}

	@Test
	void mapToErrand() throws Exception {
		// Arrange
		final var priority = "MEDIUM";
		final var category = "category";
		final var type = "type";
		final var partyId = "partyId";
		final var labelId_1 = "labelId_1";
		final var labelId_2 = "labelId_2";
		final var labelId_3 = "labelId_3";
		final var label_1 = "label_1";
		final var label_2 = "label_2";
		final var label_3 = "label_3";
		final var labels = List.of(label_1, label_2);
		final var namespace = "namespace";
		final var familyId = "789";
		final var resourceName = "resourceName";
		final var classification = "classification";
		final var displayName = "displayName";
		final var caseMetaDataEntity = new CaseMetaDataEntity().withNamespace(namespace).withFamilyId(familyId);

		when(propertiesMock.getPriority()).thenReturn(priority);
		when(propertiesMock.getCategory()).thenReturn(category);
		when(propertiesMock.getType()).thenReturn(type);
		when(propertiesMock.getFamilyId()).thenReturn(familyId);
		when(caseMetaDataRepositoryMock.findByFamilyId(familyId)).thenReturn(caseMetaDataEntity);
		when(propertiesMock.getLabels()).thenReturn(labels);
		when(labelsProviderMock.getLabels(namespace)).thenReturn(List.of(
			new Label().id(labelId_1).resourcePath(label_1).resourceName(resourceName).classification(classification).displayName(displayName)
				.labels(List.of(new Label().id(labelId_2).resourcePath(label_2).resourceName(resourceName).classification(classification).displayName(displayName)
					.labels(List.of(new Label().id(labelId_3).resourcePath(label_3).resourceName(resourceName).classification(classification).displayName(displayName)))))));

		when(propertiesMock.getPriority()).thenReturn(priority);
		when(propertiesMock.getCategory()).thenReturn(category);
		when(propertiesMock.getType()).thenReturn(type);
		when(partyClientMock.getPartyId(anyString(), any(), anyString())).thenReturn(Optional.of(partyId));

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
			Stakeholder::getExternalIdType,
			Stakeholder::getExternalId,
			Stakeholder::getContactChannels,
			Stakeholder::getOrganizationName,
			Stakeholder::getParameters)
			.containsExactly(
				tuple(ROLE_APPLICANT,
					"Kalle",
					"Anka",
					"PRIVATE",
					partyId,
					List.of(new ContactChannel().type("Email").value("kalle.anka@sundsvall.se"),
						new ContactChannel().type("Phone").value("0701112223")),
					null,
					emptyList()));

		assertThat(errand.getLabels()).extracting(
			ErrandLabel::getId).containsExactly(labelId_1, labelId_2);

		assertThat(errand.getExternalTags()).containsExactlyInAnyOrder(
			new ExternalTag().key("caseId").value("6911"),
			new ExternalTag().key("familyId").value("197"));

		assertThat(errand.getReporterUserId()).isEqualTo("Kalle Anka-kalle.anka@sundsvall.se");

		verify(propertiesMock).getPriority();
		verify(propertiesMock).getCategory();
		verify(propertiesMock).getType();
		verify(partyClientMock).getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, "123456789012");
		verifyNoMoreInteractions(propertiesMock, partyClientMock);
	}
}
