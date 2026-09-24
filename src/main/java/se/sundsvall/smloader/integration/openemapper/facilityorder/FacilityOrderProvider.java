package se.sundsvall.smloader.integration.openemapper.facilityorder;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.openemapper.LabelsMapper;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperBase;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.util.LabelsProvider;

import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OPERATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OPERATION_INFORMATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ORDER_DETAILS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_PROPERTY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ROOM;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_USER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_WORKPLACE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OPERATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OPERATION_INFORMATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ORDER_DETAILS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_PROPERTY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ROOM;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_USER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_WORKPLACE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_FACILITY_ORDER;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;
import static se.sundsvall.smloader.service.mapper.SupportManagementMapper.toParameter;

@Component
class FacilityOrderProvider extends OpenEMapperBase {

	private final OpenEMapperProperties properties;

	private final LabelsProvider labelsProvider;

	private final CaseMetaDataRepository caseMetaDataRepository;

	public FacilityOrderProvider(final @Qualifier("facilityorder") OpenEMapperProperties properties, final LabelsProvider labelsProvider, final CaseMetaDataRepository caseMetaDataRepository) {
		this.properties = properties;
		this.labelsProvider = labelsProvider;
		this.caseMetaDataRepository = caseMetaDataRepository;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, FacilityOrder.class);

		final var caseMetaDataEntity = caseMetaDataRepository.findByFamilyId(properties.getFamilyId());

		final var errandLabels = LabelsMapper.mapLabels(labelsProvider.getLabels(caseMetaDataEntity.getNamespace()), properties.getLabels());

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_FACILITY_ORDER)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(errandLabels)
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(result.applicantUserId());
	}

	private List<Stakeholder> getStakeholders(final FacilityOrder facilityOrder) {
		return List.of(new Stakeholder()
			.role(ROLE_APPLICANT)
			.firstName(facilityOrder.applicantFirstname())
			.lastName(facilityOrder.applicantLastname())
			.contactChannels(getContactChannels(facilityOrder))
			.parameters(Stream.of(
				toParameter(KEY_ADMINISTRATION_NAME, facilityOrder.applicantOrganization(), DISPLAY_ADMINISTRATION_NAME),
				toParameter(KEY_TITLE, facilityOrder.applicantTitle(), DISPLAY_TITLE),
				toParameter(KEY_USER_ID, facilityOrder.applicantUserId(), DISPLAY_USER_ID))
				.filter(Objects::nonNull)
				.toList()));
	}

	private List<ContactChannel> getContactChannels(final FacilityOrder facilityOrder) {
		final var contactChannels = new ArrayList<>(Optional.ofNullable(getContactChannels(facilityOrder.applicantEmail(), facilityOrder.applicantPhone())).orElseGet(ArrayList::new));

		if (facilityOrder.applicantMobilePhone() != null) {
			contactChannels.add(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_PHONE)
				.value(facilityOrder.applicantMobilePhone()));
		}

		return contactChannels;
	}

	private List<Parameter> getParameters(final FacilityOrder facilityOrder) {
		return Stream.of(
			singleParameter(facilityOrder.administration(), KEY_ADMINISTRATION, DISPLAY_ADMINISTRATION),
			singleParameter(facilityOrder.operation(), KEY_OPERATION, DISPLAY_OPERATION),
			singleParameter(facilityOrder.orderDetails(), KEY_ORDER_DETAILS, DISPLAY_ORDER_DETAILS),
			singleParameter(facilityOrder.property(), KEY_PROPERTY, DISPLAY_PROPERTY),
			singleParameter(facilityOrder.workplaceUnit(), KEY_WORKPLACE_UNIT, DISPLAY_WORKPLACE_UNIT),
			singleParameter(facilityOrder.room(), KEY_ROOM, DISPLAY_ROOM),
			singleParameter(facilityOrder.operationInformation(), KEY_OPERATION_INFORMATION, DISPLAY_OPERATION_INFORMATION))
			.filter(Objects::nonNull)
			.toList();
	}
}
