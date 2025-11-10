package se.sundsvall.smloader.integration.openemapper.substitutemanager;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_END_DATE_CERTIFY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_RESPONSIBILITY_NUMBER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_START_DATE_CERTIIFY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_RESPONSIBILITY_NUMBER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPROVER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_SUBSTITUTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_SUBSTITUTE_MANAGER;
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
class SubstituteManagerProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	private final LabelsProvider labelsProvider;

	private final CaseMetaDataRepository caseMetaDataRepository;

	public SubstituteManagerProvider(final @Qualifier("substitutemanager") OpenEMapperProperties properties, final PartyClient partyClient, final LabelsProvider labelsProvider,
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
		final var result = extractValue(xml, SubstituteManager.class);

		final var caseMetaDataEntity = caseMetaDataRepository.findByFamilyId(properties.getFamilyId());

		final var errandLabels = properties.getLabels().stream()
			.map(sourcePath -> labelsProvider.getLabel(caseMetaDataEntity.getNamespace(), sourcePath))
			.map(LabelMapper::toErrandLabel)
			.toList();

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_SUBSTITUTE_MANAGER)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(errandLabels)
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(List.of(new Parameter().key(KEY_RESPONSIBILITY_NUMBER).addValuesItem(result.responsibilityNumber()).displayName(DISPLAY_RESPONSIBILITY_NUMBER),
				new Parameter().key(KEY_START_DATE).addValuesItem(result.startDate()).displayName(DISPLAY_START_DATE_CERTIIFY),
				new Parameter().key(KEY_END_DATE).addValuesItem(result.endDate()).displayName(DISPLAY_END_DATE_CERTIFY)))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(getReporterUserId(result));
	}

	private List<Stakeholder> getStakeholders(final SubstituteManager substituteManager) {
		return List.of(new Stakeholder()
			.role(ROLE_APPLICANT)
			.firstName(substituteManager.applicantFirstname())
			.lastName(substituteManager.applicantLastname())
			.parameters(toParameterList(KEY_ADMINISTRATION_NAME, substituteManager.applicantOrganization()))
			.contactChannels(getContactChannels(substituteManager.applicantEmail())),
			new Stakeholder()
				.role(ROLE_SUBSTITUTE)
				.firstName(substituteManager.substituteManagerFirstname())
				.lastName(substituteManager.substituteManagerLastname())
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, substituteManager.substituteManagerOrganization()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(substituteManager.substituteManagerLegalId())),
			getManagerStakeholder(substituteManager),
			new Stakeholder()
				.role(ROLE_APPROVER)
				.firstName(substituteManager.approvingManagerFirstname())
				.lastName(substituteManager.approvingManagerLastname())
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, substituteManager.approvingManagerOrganization()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(substituteManager.approvingManagerLegalId())));
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private Stakeholder getManagerStakeholder(final SubstituteManager substituteManager) {
		return isNotEmpty(substituteManager.managerFirstname()) ? new Stakeholder()
			.role(ROLE_MANAGER)
			.firstName(substituteManager.managerFirstname())
			.lastName(substituteManager.managerLastname())
			.parameters(toParameterList(KEY_ADMINISTRATION_NAME, substituteManager.managerOrganization()))
			.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
			.externalId(getPartyId(substituteManager.managerLegalId()))
			: new Stakeholder()
				.role(ROLE_MANAGER)
				.firstName(substituteManager.otherSenderFirstname())
				.lastName(substituteManager.otherSenderLastname())
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, substituteManager.otherSenderOrganization()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(substituteManager.otherSenderLegalId()));
	}

	private String getPartyId(final String legalId) {
		return isNotEmpty(legalId) ? partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null) : null;
	}

	private String getReporterUserId(final SubstituteManager substituteManager) {
		return !isEmpty(substituteManager.managerUserId()) ? substituteManager.managerUserId() : substituteManager.otherSenderUserId();
	}
}
