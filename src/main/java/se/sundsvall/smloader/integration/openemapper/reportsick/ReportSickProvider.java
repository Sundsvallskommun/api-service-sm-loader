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
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.EXTERNAL_ID_TYPE_PRIVATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
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
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(xml, result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId())))
			.reporterUserId(getReporterUserId(result));
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
		return partyClient.getPartyId(MUNICIPALITY_ID, PartyType.PRIVATE, legalId).orElse(null);
	}

	private String getReporterUserId(final ReportSick reportSick) {
		return reportSick.posterFirstname() + " " + reportSick.posterLastname() + "-" + reportSick.posterEmail();
	}

	private List<Parameter> getParameters(final byte[] xml, final ReportSick reportSick) {

		var parameters = new ArrayList<Parameter>();

		parameters.add(new Parameter().key("administrativeUnit").values(List.of(reportSick.administrativeUnit())));
		parameters.add(new Parameter().key("employmentType").values(List.of(reportSick.employmentType())));

		final int countOfSickLeavePeriods = Optional.ofNullable(reportSick.countOfSickLeavePeriods()).orElse(0);

		if (countOfSickLeavePeriods == 0) {
			return parameters;
		}

		parameters.addAll(getSickLeaveParameters(xml, countOfSickLeavePeriods));

		return parameters;
	}

	private List<Parameter> getSickLeaveParameters(final byte[] xml, final int countOfSickLeavePeriods) {

		var parameters = new ArrayList<Parameter>();

		var sickNotePercentRows = new ArrayList<String>();

		var sickNoteStartDateRows = new ArrayList<String>();

		var sickNoteEndDateRows = new ArrayList<String>();

		for (int i = 1; i <= countOfSickLeavePeriods; i++) {
			var pathPercent = "/FlowInstance/Values/sickNotePercentRow" + i + "/Value";
			var pathStartDate = "/FlowInstance/Values/sickNotePeriodRow" + i + "/Datum_fran";
			var pathEndDate = "/FlowInstance/Values/sickNotePeriodRow" + i + "/Datum_fran";

			sickNotePercentRows.add(evaluateXPath(xml, pathPercent).text());
			sickNoteStartDateRows.add(evaluateXPath(xml, pathStartDate).text());
			sickNoteEndDateRows.add(evaluateXPath(xml, pathEndDate).text());
		}

		parameters.add(new Parameter().key("sickNotePercentages").values(sickNotePercentRows));
		parameters.add(new Parameter().key("sickNoteStartDates").values(sickNoteStartDateRows));
		parameters.add(new Parameter().key("sickNoteEndDates").values(sickNoteEndDateRows));
		return parameters;
	}
}
