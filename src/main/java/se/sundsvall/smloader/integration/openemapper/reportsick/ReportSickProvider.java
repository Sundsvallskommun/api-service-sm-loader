package se.sundsvall.smloader.integration.openemapper.reportsick;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.db.CaseMetaDataRepository;
import se.sundsvall.smloader.integration.openemapper.LabelsMapper;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.party.PartyClient;
import se.sundsvall.smloader.integration.util.LabelsProvider;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

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
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SICK_NOTES;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SICK_PERIODS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.GROUP_SICK_NOTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.GROUP_SICK_PERIOD;
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
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATIVE_UNIT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_EMPLOYEE_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_EMPLOYMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_HAVE_SICK_NOTE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SICK_NOTES;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SICK_PERIODS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.MUNICIPALITY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_EMPLOYEE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_REPORT_SICK;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;
import static se.sundsvall.smloader.service.mapper.SupportManagementMapper.toParameterList;

@Component
class ReportSickProvider implements OpenEMapper {

	private static final String VALUE_PATH = "/Value";
	private static final String SICK_NOTE_ROW = "%s|%s|%s|%s|%s";
	private static final String SICK_PERIOD_ROW = "%s|%s|%s";
	private final OpenEMapperProperties properties;

	private final PartyClient partyClient;

	private final LabelsProvider labelsProvider;

	private final CaseMetaDataRepository caseMetaDataRepository;

	public ReportSickProvider(final @Qualifier("reportsick") OpenEMapperProperties properties, final PartyClient partyClient, final LabelsProvider labelsProvider,
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
		final var result = extractValue(xml, ReportSick.class);

		final var caseMetaDataEntity = caseMetaDataRepository.findByFamilyId(properties.getFamilyId());

		final var errandLabels = LabelsMapper.mapLabels(labelsProvider.getLabels(caseMetaDataEntity.getNamespace()), properties.getLabels());

		return new Errand()
			.status(STATUS_NEW)
			.title(TITLE_REPORT_SICK)
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(new Classification().category(properties.getCategory()).type(properties.getType()))
			.labels(errandLabels)
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(xml, result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(result.applicantUserId());
	}

	private List<Stakeholder> getStakeholders(final ReportSick reportSick) {
		return List.of(
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(reportSick.applicantFirstname())
				.lastName(reportSick.applicantLastname())
				.contactChannels(getContactChannels(reportSick.applicantEmail(), reportSick.applicantPhone()))
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, reportSick.applicantOrganization())),
			new Stakeholder()
				.role(ROLE_EMPLOYEE)
				.firstName(reportSick.employeeFirstname())
				.lastName(reportSick.employeeLastname())
				.externalIdType(EXTERNAL_ID_TYPE_PRIVATE)
				.externalId(getPartyId(reportSick.employeeLegalId()))
				.parameters(toParameterList(KEY_ADMINISTRATION_NAME, reportSick.employeeOrganization())));
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
		Optional.ofNullable(reportSick.employeeTitle()).ifPresent(employeeTitle -> parameters.add(new Parameter().key(KEY_EMPLOYEE_TITLE).values(List.of(employeeTitle))
			.displayName(DISPLAY_EMPLOYEE_TITLE)));
		Optional.ofNullable(reportSick.absentType()).ifPresent(absentType -> parameters.add(new Parameter().key(KEY_ABSENT_TYPE).values(List.of(absentType))
			.displayName(DISPLAY_ABSENT_TYPE).group(GROUP_SICK_NOTE)));
		Optional.ofNullable(reportSick.absentFirstDay()).ifPresent(absentFirstDay -> parameters.add(new Parameter().key(KEY_ABSENT_FIRST_DAY).values(List.of(absentFirstDay))
			.displayName(DISPLAY_ABSENT_FIRST_DAY).group(GROUP_SICK_NOTE)));
		Optional.ofNullable(reportSick.absentStartDate()).ifPresent(absentStartDate -> parameters.add(new Parameter().key(KEY_ABSENT_START_DATE).values(List.of(absentStartDate))
			.displayName(DISPLAY_ABSENT_START_DATE)));
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
		Optional.ofNullable(reportSick.haveSickNote()).ifPresent(haveSickNote -> parameters.add(new Parameter().key(KEY_HAVE_SICK_NOTE).values(List.of(haveSickNote))
			.displayName(DISPLAY_HAVE_SICK_NOTE)));

		final var sickPeriods = parsePeriods(xml);

		final var sickPeriodRows = new ArrayList<String>();

		sickPeriods.forEach(sickPeriod -> sickPeriodRows.add(String.format(SICK_PERIOD_ROW,
			Optional.ofNullable(sickPeriod.date()).orElse(""),
			Optional.ofNullable(sickPeriod.startTime()).orElse(""),
			Optional.ofNullable(sickPeriod.endTime()).orElse(""))));

		if (!sickPeriods.isEmpty()) {
			parameters.add(new Parameter().key(KEY_SICK_PERIODS).values(sickPeriodRows).displayName(DISPLAY_SICK_PERIODS).group(GROUP_SICK_PERIOD));
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

		final var sickNoteRows = new ArrayList<String>();

		for (int i = 1; i <= countOfSickLeavePeriods; i++) {
			final var pathPercent = "/FlowInstance/Values/sickNotePercentRow" + i + VALUE_PATH;
			final var pathStartDate = "/FlowInstance/Values/sickNotePeriodRow" + i + "/Datum_fran";
			final var pathEndDate = "/FlowInstance/Values/sickNotePeriodRow" + i + "/Datum_till";
			final var pathTimeCare = "/FlowInstance/Values/timeCareRow" + i + VALUE_PATH;
			final var pathCurrentSchedule = "/FlowInstance/Values/currentScheduleRow" + i + VALUE_PATH;

			sickNoteRows.add(String.format(SICK_NOTE_ROW,
				evaluateXPath(xml, pathStartDate).text(),
				evaluateXPath(xml, pathEndDate).text(),
				evaluateXPath(xml, pathPercent).text(),
				evaluateXPath(xml, pathTimeCare).text(),
				evaluateXPath(xml, pathCurrentSchedule).text()));
		}

		parameters.add(new Parameter().key(KEY_SICK_NOTES).values(sickNoteRows).displayName(DISPLAY_SICK_NOTES).group(GROUP_SICK_NOTE));

		return parameters;
	}

	public static List<SickPeriod> parsePeriods(byte[] xml) {
		final var elements = evaluateXPath(xml, "/FlowInstance/Values/absentDateTable/Row");

		final var periods = new ArrayList<SickPeriod>();

		elements.forEach(element -> periods.add(new SickPeriod(
			evaluateXPath(element, "/Column/Value").text(),
			evaluateXPath(element, "/Column1/Value").text(),
			evaluateXPath(element, "/Column2/Value").text())));

		return periods;
	}
}
