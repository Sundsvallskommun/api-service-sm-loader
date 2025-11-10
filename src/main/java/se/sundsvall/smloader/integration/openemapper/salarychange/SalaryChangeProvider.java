package se.sundsvall.smloader.integration.openemapper.salarychange;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_AMOUNT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_FROM_MONTH;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_AMOUNT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FROM_MONTH;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_SALARY_CHANGE;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;
import static se.sundsvall.smloader.service.mapper.SupportManagementMapper.toParameterList;

import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.openemapper.LabelMapper;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.integration.util.LabelsProvider;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

@Component
class SalaryChangeProvider implements OpenEMapper {
	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	private final LabelsProvider labelsProvider;

	private final CaseMetaDataRepository caseMetaDataRepository;

	public SalaryChangeProvider(final @Qualifier("salarychange") OpenEMapperProperties properties, final PartyClient partyClient, final LabelsProvider labelsProvider,
		final CaseMetaDataRepository caseMetaDataRepository) {
		this.properties = properties;
		this.partyClient = partyClient;
		this.labelsProvider = labelsProvider;
		this.caseMetaDataRepository = caseMetaDataRepository;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, SalaryChange.class);

		final var caseMetaDataEntity = caseMetaDataRepository.findByFamilyId(properties.getFamilyId());

		final var errandLabels = properties.getLabels().stream()
			.map(sourcePath -> labelsProvider.getLabel(caseMetaDataEntity.getNamespace(), sourcePath))
			.map(LabelMapper::toErrandLabel)
			.toList();

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_SALARY_CHANGE)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(errandLabels)
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(List.of(new Parameter().key(KEY_AMOUNT).addValuesItem(result.amount()).displayName(DISPLAY_AMOUNT),
				new Parameter().key(KEY_FROM_MONTH).addValuesItem(result.fromMonth()).displayName(DISPLAY_FROM_MONTH)))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(result.applicantUserId());
	}

	private List<Stakeholder> getStakeholders(final SalaryChange salaryChange) {
		return List.of(new Stakeholder()
			.role(ROLE_APPLICANT)
			.firstName(salaryChange.applicantFirstname())
			.lastName(salaryChange.applicantLastname())
			.parameters(toParameterList(KEY_ADMINISTRATION_NAME, salaryChange.applicantOrganization()))
			.contactChannels(getContactChannels(salaryChange.applicantEmail()))
			.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
			.externalId(getPartyId(salaryChange.applicantLegalId())));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private String getPartyId(final String legalId) {
		return isNotEmpty(legalId) ? partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null) : null;
	}
}
