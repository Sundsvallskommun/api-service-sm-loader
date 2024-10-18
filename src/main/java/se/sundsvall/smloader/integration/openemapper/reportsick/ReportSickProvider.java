package se.sundsvall.smloader.integration.openemapper.reportsick;

import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_CONTINUATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_DESCRIPTION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_FIRST_DAY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_LATE_START_TIME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_PERIOD_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_PERIOD_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_START_TIME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ABSENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_EMPLOYEE_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_EMPLOYMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_HAVE_SICK_NOTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_CONTINUATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_DESCRIPTION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_FIRST_DAY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_LATE_START_TIME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_PERIOD_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_PERIOD_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_START_TIME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ABSENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_EMPLOYEE_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_EMPLOYMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_HAVE_SICK_NOTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_CONTACT_PERSON;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_REPORT_SICK;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;

@Component
class ReportSickProvider implements OpenEMapper {

	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	public ReportSickProvider(final @Qualifier("reportsick") OpenEMapperProperties properties, final PartyClient partyClient) {
		this.properties = properties;
		this.partyClient = partyClient;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, ReportSick.class);

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_REPORT_SICK)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(properties.getLabels())
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(xml, result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(result.applicantUserId());
	}

	private List<Stakeholder> getStakeholders(final ReportSick reportSick) {
		return List.of(new Stakeholder()
				.role(ROLE_CONTACT_PERSON)
				.firstName(reportSick.posterFirstname())
				.lastName(reportSick.posterLastname())
				.contactChannels(getContactChannels(reportSick.posterEmail(), null)),
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(reportSick.applicantFirstname())
				.lastName(reportSick.applicantLastname())
				.contactChannels(getContactChannels(reportSick.applicantEmail(), reportSick.applicantPhone()))
				.organizationName(reportSick.applicantOrganization()),
			new Stakeholder()
				.role(ROLE_EMPLOYEE)
				.firstName(reportSick.employeeFirstname())
				.lastName(reportSick.employeeLastname())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(reportSick.employeeLegalId()))
				.organizationName(reportSick.employeeOrganization()));
	}

	private List<ContactChannel> getContactChannels(final String email, final String phone) {
		final var contactChannels = new ArrayList<ContactChannel>();

		contactChannels.add(new ContactChannel()
			.type(CONTACT_CHANNEL_TYPE_EMAIL)
			.value(email));

		if (!isNull(phone)) {
			contactChannels.add(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_PHONE)
				.value(phone));
		}
		return contactChannels;
	}

	private String getPartyId(final String legalId) {
		return isNotEmpty(legalId) ? partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null) : null;
	}

	private List<Parameter> getParameters(final byte[] xml, final ReportSick reportSick) {

		final var parameters = new ArrayList<Parameter>();

		Optional.ofNullable(reportSick.administrativeUnit()).ifPresent(administrativeUnit -> parameters.add(new Parameter().key(KEY_ADMINISTRATIVE_UNIT).values(List.of(administrativeUnit))
			.displayName(DISPLAY_ADMINISTRATIVE_UNIT)));
		Optional.ofNullable(reportSick.employmentType()).ifPresent(employmentType -> parameters.add(new Parameter().key(KEY_EMPLOYMENT_TYPE).values(List.of(employmentType))
			.displayName(DISPLAY_EMPLOYMENT_TYPE)));
		Optional.ofNullable(reportSick.absentStartDate()).ifPresent(absentStartDate -> parameters.add(new Parameter().key(KEY_ABSENT_START_DATE).values(List.of(absentStartDate))
			.displayName(DISPLAY_ABSENT_START_DATE)));
		Optional.ofNullable(reportSick.employeeTitle()).ifPresent(employeeTitle -> parameters.add(new Parameter().key(KEY_EMPLOYEE_TITLE).values(List.of(employeeTitle))
			.displayName(DISPLAY_EMPLOYEE_TITLE)));
		Optional.ofNullable(reportSick.absentDescription()).ifPresent(absentDescription -> parameters.add(new Parameter().key(KEY_ABSENT_DESCRIPTION).values(List.of(absentDescription))
			.displayName(DISPLAY_ABSENT_DESCRIPTION)));
		Optional.ofNullable(reportSick.absentStartTime()).ifPresent(absentStartTime -> parameters.add(new Parameter().key(KEY_ABSENT_START_TIME).values(List.of(absentStartTime))
			.displayName(DISPLAY_ABSENT_START_TIME)));
		Optional.ofNullable(reportSick.absentLateStartTime()).ifPresent(absentLateStartTime -> parameters.add(new Parameter().key(KEY_ABSENT_LATE_START_TIME).values(List.of(absentLateStartTime))
			.displayName(DISPLAY_ABSENT_LATE_START_TIME)));
		Optional.ofNullable(reportSick.absentContinuation()).ifPresent(absentContinuation -> parameters.add(new Parameter().key(KEY_ABSENT_CONTINUATION).values(List.of(absentContinuation))
			.displayName(DISPLAY_ABSENT_CONTINUATION)));
		Optional.ofNullable(reportSick.absentPeriodStartDate()).ifPresent(absentPeriodStartDate -> parameters.add(new Parameter().key(KEY_ABSENT_PERIOD_START_DATE).values(List.of(absentPeriodStartDate))
			.displayName(DISPLAY_ABSENT_PERIOD_START_DATE)));
		Optional.ofNullable(reportSick.absentPeriodEndDate()).ifPresent(absentPeriodEndDate -> parameters.add(new Parameter().key(KEY_ABSENT_PERIOD_END_DATE).values(List.of(absentPeriodEndDate))
			.displayName(DISPLAY_ABSENT_PERIOD_END_DATE)));
		Optional.ofNullable(reportSick.absentType()).ifPresent(absentType -> parameters.add(new Parameter().key(KEY_ABSENT_TYPE).values(List.of(absentType))
			.displayName(DISPLAY_ABSENT_TYPE)));
		Optional.ofNullable(reportSick.absentFirstDay()).ifPresent(absentFirstDay -> parameters.add(new Parameter().key(KEY_ABSENT_FIRST_DAY).values(List.of(absentFirstDay))
			.displayName(DISPLAY_ABSENT_FIRST_DAY)));
		Optional.ofNullable(reportSick.haveSickNote()).ifPresent(haveSickNote -> parameters.add(new Parameter().key(KEY_HAVE_SICK_NOTE).values(List.of(haveSickNote))
			.displayName(DISPLAY_HAVE_SICK_NOTE)));

		var sickPeriodDates = new ArrayList<String>();

		var sickPeriodStartTimes = new ArrayList<String>();

		var sickPeriodEndTimes = new ArrayList<String>();

		final var sickPeriods = parsePeriods(xml);

		sickPeriods.forEach(sickPeriod -> {
			sickPeriodDates.add(sickPeriod.date());
			sickPeriodStartTimes.add(sickPeriod.startTime());
			sickPeriodEndTimes.add(sickPeriod.endTime());
		});

		if (!sickPeriodDates.isEmpty()) {
			parameters.add(new Parameter().key("sickPeriodDates").values(sickPeriodDates).displayName("Sjukperiodens datum"));
			parameters.add(new Parameter().key("sickPeriodStartTimes").values(sickPeriodStartTimes).displayName("Sjukperiodens starttider"));
			parameters.add(new Parameter().key("sickPeriodEndTimes").values(sickPeriodEndTimes).displayName("Sjukperiodens sluttider"));
		}

		final int countOfSickLeavePeriods = Optional.ofNullable(reportSick.countOfSickLeavePeriods()).orElse(0);

		if (countOfSickLeavePeriods == 0) {
			return parameters;
		}

		parameters.addAll(getSickLeaveParameters(xml, countOfSickLeavePeriods));

		return parameters;
	}

	private List<Parameter> getSickLeaveParameters(final byte[] xml, final int countOfSickLeavePeriods) {

		final var parameters = new ArrayList<Parameter>();

		final var sickNotePercentRows = new ArrayList<String>();

		final var sickNoteStartDateRows = new ArrayList<String>();

		final var sickNoteEndDateRows = new ArrayList<String>();

		for (int i = 1; i <= countOfSickLeavePeriods; i++) {
			final var pathPercent = "/FlowInstance/Values/sickNotePercentRow" + i + "/Value";
			final var pathStartDate = "/FlowInstance/Values/sickNotePeriodRow" + i + "/Datum_fran";
			final var pathEndDate = "/FlowInstance/Values/sickNotePeriodRow" + i + "/Datum_fran";

			sickNotePercentRows.add(evaluateXPath(xml, pathPercent).text());
			sickNoteStartDateRows.add(evaluateXPath(xml, pathStartDate).text());
			sickNoteEndDateRows.add(evaluateXPath(xml, pathEndDate).text());
		}

		parameters.add(new Parameter().key("sickNotePercentages").values(sickNotePercentRows).displayName("Sjukskrivningsgrad i procent"));
		parameters.add(new Parameter().key("sickNoteStartDates").values(sickNoteStartDateRows).displayName("Sjukskrivnings startdatum"));
		parameters.add(new Parameter().key("sickNoteEndDates").values(sickNoteEndDateRows).displayName("Sjukskrivnings slutdatum"));
		return parameters;
	}

	public static List<SickPeriod> parsePeriods(byte[] xml) {
		final var elements = evaluateXPath(xml, "/FlowInstance/Values/absentDateTable/Row");

		final var periods = new ArrayList<SickPeriod>();

		elements.forEach(element ->
			periods.add(new SickPeriod(
				evaluateXPath(element, "/Column/Value").text(),
				evaluateXPath(element, "/Column1/Value").text(),
				evaluateXPath(element, "/Column2/Value").text())
		));

		return periods;
	}
}
