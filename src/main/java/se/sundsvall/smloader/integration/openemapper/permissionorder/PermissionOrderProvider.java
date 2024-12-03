package se.sundsvall.smloader.integration.openemapper.permissionorder;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ACCESS_TYPE_HEROMA;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_BOU;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_IAF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_KOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_KSK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_LMK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_MK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_OFK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_SBK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT_PART_VOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_COMPUTER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_IS_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_NAME_TERMINATED;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_NOT_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_BOU;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_IAF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_KSK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_LMK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_MK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_OFK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_SBK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_OTHER_UNITS_VOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_STILL_EMPLOYED;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SYSTEM_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_TYPE_OF_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_UNIT_BOU;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_UNIT_KOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_UNIT_VOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_UPDATE_DESCRIPTION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_USER_TYPE_HEROMA;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ACCESS_TYPE_HEROMA;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_BOU;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_IAF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_KOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_KSK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_LMK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_MK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_OFK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_SBK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT_PART_VOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_COMPUTER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_IS_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_NAME_TERMINATED;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_NOT_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_BOU;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_IAF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_KSK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_LMK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_MK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_OFK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_SBK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_OTHER_UNITS_VOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_STILL_EMPLOYED;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SYSTEM_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_TYPE_OF_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UNIT_BOU;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UNIT_KOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UNIT_VOF;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UPDATE_DESCRIPTION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_USER_TYPE_HEROMA;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_USER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_PERMISSION_ORDER;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

@Component
class PermissionOrderProvider implements OpenEMapper {

	private static final String BASE_XPATH = "/FlowInstance/Values/";

	private final OpenEMapperProperties properties;
	private final PartyClient partyClient;

	public PermissionOrderProvider(final @Qualifier("permissionorder") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, PermissionOrder.class);

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_PERMISSION_ORDER)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(List.of(properties.getCategory(), properties.getType()))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result, xml))
			.description(result.otherInformation())
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(result.applicantUserId());
	}

	private List<Stakeholder> getStakeholders(final PermissionOrder permissionOrder) {
		final var stakeholders = new ArrayList<>(List.of(new Stakeholder()
			.role(ROLE_CONTACT_PERSON)
			.firstName(permissionOrder.posterFirstname())
			.lastName(permissionOrder.posterLastname())
			.contactChannels(getContactChannels(permissionOrder.posterEmail())),
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(permissionOrder.applicantFirstname())
				.lastName(permissionOrder.applicantLastname())
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, permissionOrder.applicantOrganization()))
				.contactChannels(getContactChannels(permissionOrder.applicantEmail())),
			new Stakeholder()
				.role(ROLE_USER)
				.firstName(permissionOrder.userFirstname())
				.lastName(permissionOrder.userLastname())
				.contactChannels(getContactChannels(permissionOrder.userEmail()))
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, permissionOrder.userOrganization()))
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(permissionOrder.userLegalId()))));
		if (isNotEmpty(permissionOrder.managerFirstname())) {
			stakeholders.add(new Stakeholder()
				.role(ROLE_MANAGER)
				.firstName(permissionOrder.managerFirstname())
				.lastName(permissionOrder.managerLastname())
				.contactChannels(getContactChannels(permissionOrder.managerEmail()))
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, permissionOrder.managerOrganization())));
		}
		return stakeholders;
	}

	private List<ContactChannel> getContactChannels(final String email) {
		return List.of(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));
	}

	private String getPartyId(final String legalId) {
		return isNotEmpty(legalId) ? partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null) : null;
	}

	private List<Parameter> getParameters(final PermissionOrder permissionOrder, final byte[] xml) {
		final var parameters = new ArrayList<Parameter>();

		Optional.ofNullable(permissionOrder.computerId()).ifPresent(computerId -> parameters.add(new Parameter().key(KEY_COMPUTER_ID).addValuesItem(computerId)
			.displayName(DISPLAY_COMPUTER_ID)));
		Optional.ofNullable(permissionOrder.typeOfAccess()).ifPresent(typeOfAccess -> parameters.add(new Parameter().key(KEY_TYPE_OF_ACCESS).addValuesItem(typeOfAccess)
			.displayName(DISPLAY_TYPE_OF_ACCESS)));
		Optional.ofNullable(permissionOrder.systemAccess()).ifPresent(systemAccess -> parameters.add(new Parameter().key(KEY_SYSTEM_ACCESS).addValuesItem(systemAccess)
			.displayName(DISPLAY_SYSTEM_ACCESS)));
		Optional.ofNullable(permissionOrder.startDate()).ifPresent(startDate -> parameters.add(new Parameter().key(KEY_START_DATE).addValuesItem(startDate)
			.displayName(DISPLAY_START_DATE)));
		Optional.ofNullable(permissionOrder.isManager()).ifPresent(isManager -> parameters.add(new Parameter().key(KEY_IS_MANAGER).addValuesItem(isManager)
			.displayName(DISPLAY_IS_MANAGER)));
		Optional.ofNullable(permissionOrder.notEmployee()).ifPresent(notEmployee -> parameters.add(new Parameter().key(KEY_NOT_EMPLOYEE).addValuesItem(notEmployee)
			.displayName(DISPLAY_NOT_EMPLOYEE)));
		Optional.ofNullable(permissionOrder.userTypeHeroma()).ifPresent(userTypeHeroma -> parameters.add(new Parameter().key(KEY_USER_TYPE_HEROMA).addValuesItem(userTypeHeroma)
			.displayName(DISPLAY_USER_TYPE_HEROMA)));
		Optional.ofNullable(permissionOrder.accessTypeHeroma()).ifPresent(accessTypeHeroma -> parameters.add(new Parameter().key(KEY_ACCESS_TYPE_HEROMA).addValuesItem(accessTypeHeroma)
			.displayName(DISPLAY_ACCESS_TYPE_HEROMA)));
		Optional.ofNullable(permissionOrder.updateDescription()).ifPresent(updateDescription -> parameters.add(new Parameter().key(KEY_UPDATE_DESCRIPTION).addValuesItem(updateDescription)
			.displayName(DISPLAY_UPDATE_DESCRIPTION)));
		Optional.ofNullable(permissionOrder.stillEmployed()).ifPresent(stillEmployed -> parameters.add(new Parameter().key(KEY_STILL_EMPLOYED).addValuesItem(stillEmployed)
			.displayName(DISPLAY_STILL_EMPLOYED)));
		Optional.ofNullable(permissionOrder.nameTerminated()).ifPresent(nameTerminated -> parameters.add(new Parameter().key(KEY_NAME_TERMINATED).addValuesItem(nameTerminated)
			.displayName(DISPLAY_NAME_TERMINATED)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT)).ifPresent(units -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT).values(units)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_BOU)).ifPresent(partsBou -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_BOU).values(partsBou)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_BOU)));
		Optional.ofNullable(getValuesFromXPath(xml, KEY_UNIT_BOU)).ifPresent(unitsBou -> parameters.add(new Parameter().key(KEY_UNIT_BOU).values(unitsBou)
			.displayName(DISPLAY_UNIT_BOU)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_BOU)).ifPresent(otherUnitsBou -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_BOU).values(otherUnitsBou)
			.displayName(DISPLAY_OTHER_UNITS_BOU)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_KOF)).ifPresent(partsKof -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_KOF).values(partsKof)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_KOF)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_UNIT_KOF)).ifPresent(unitsKof -> parameters.add(new Parameter().key(KEY_UNIT_KOF).values(unitsKof)
			.displayName(DISPLAY_UNIT_KOF)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_VOF)).ifPresent(partsVof -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_VOF).values(partsVof)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_VOF)));
		Optional.ofNullable(getValuesFromXPath(xml, KEY_UNIT_VOF)).ifPresent(unitsVof -> parameters.add(new Parameter().key(KEY_UNIT_VOF).values(unitsVof)
			.displayName(DISPLAY_UNIT_VOF)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_VOF)).ifPresent(otherUnitsVof -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_VOF).values(otherUnitsVof)
			.displayName(DISPLAY_OTHER_UNITS_VOF)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_IAF)).ifPresent(partsIaf -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_IAF).values(partsIaf)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_IAF)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_IAF)).ifPresent(otherUnitsIaf -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_IAF).values(otherUnitsIaf)
			.displayName(DISPLAY_OTHER_UNITS_IAF)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_KSK)).ifPresent(partsKsk -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_KSK).values(partsKsk)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_KSK)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_KSK)).ifPresent(otherUnitsKsk -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_KSK).values(otherUnitsKsk)
			.displayName(DISPLAY_OTHER_UNITS_KSK)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_SBK)).ifPresent(partsSbk -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_SBK).values(partsSbk)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_SBK)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_SBK)).ifPresent(otherUnitsSbk -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_SBK).values(otherUnitsSbk)
			.displayName(DISPLAY_OTHER_UNITS_SBK)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_LMK)).ifPresent(partsLmk -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_LMK).values(partsLmk)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_LMK)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_LMK)).ifPresent(otherUnitsLmk -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_LMK).values(otherUnitsLmk)
			.displayName(DISPLAY_OTHER_UNITS_LMK)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_MK)).ifPresent(partsMk -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_MK).values(partsMk)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_MK)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_MK)).ifPresent(otherUnitsMk -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_MK).values(otherUnitsMk)
			.displayName(DISPLAY_OTHER_UNITS_MK)));

		Optional.ofNullable(getValuesFromXPath(xml, KEY_ADMINISTRATIVE_UNIT_PART_OFK)).ifPresent(partsOfk -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT_PART_OFK).values(partsOfk)
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT_PART_OFK)));
		Optional.ofNullable(getRowsFromXPath(xml, KEY_OTHER_UNITS_OFK)).ifPresent(otherUnitsOfk -> parameters.add(new Parameter().key(KEY_OTHER_UNITS_OFK).values(otherUnitsOfk)
			.displayName(DISPLAY_OTHER_UNITS_OFK)));

		return parameters;
	}

	private List<String> getValuesFromXPath(final byte[] xml, final String xpath) {
		final var elements = evaluateXPath(xml, BASE_XPATH + xpath + "/Value");

		if (isNull(elements) || elements.isEmpty()) {
			return null;
		}

		return elements.stream().map(Element::text).toList();
	}

	private List<String> getRowsFromXPath(final byte[] xml, final String xpath) {
		final var elements = evaluateXPath(xml, BASE_XPATH + xpath + "/Row/Column/Value");

		if (isNull(elements) || elements.isEmpty()) {
			return null;
		}

		return elements.stream().map(Element::text).toList();
	}
}
