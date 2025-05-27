package se.sundsvall.smloader.service;

import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SICK_NOTES;
import static se.sundsvall.smloader.integration.util.ErrandConstants.GROUP_SICK_NOTE;

import generated.se.sundsvall.supportmanagement.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

@Service
public class MigrationService {

	// Temporary fix for sick period migration
	private static final String SICK_PERIOD_ROW = "%s|%s|%s";
	private static final String SICK_NOTE_ROW = "%s|%s|%s|%s|%s";
	private static final String KEY_SICK_PERIOD_DATES = "sickPeriodDates";
	private static final String KEY_SICK_PERIOD_START_TIMES = "sickPeriodStartTimes";
	private static final String KEY_SICK_PERIOD_END_TIMES = "sickPeriodEndTimes";
	private static final String GROUP_SICK_PERIOD = "Sjukperioder";
	private static final String KEY_SICK_PERIODS = "sickPeriods";
	private static final String DISPLAY_SICK_PERIODS = "Datum|Starttid|Sluttid";
	private static final String KEY_SICK_NOTES = "sickNotes";
	public static final String KEY_SICK_NOTE_PERCENTAGES = "sickNotePercentages";
	public static final String KEY_SICK_NOTE_START_DATES = "sickNoteStartDates";
	public static final String KEY_SICK_NOTE_END_DATES = "sickNoteEndDates";
	public static final String KEY_TIME_CARE = "timeCare";
	public static final String KEY_CURRENT_SCHEDULE = "currentSchedule";

	private final SupportManagementClient supportManagementClient;

	public MigrationService(final SupportManagementClient supportManagementClient) {
		this.supportManagementClient = supportManagementClient;
	}

	public void migrateReportSick(final String namespace, final String municipalityId) {

		final var filter = "exists(parameters.key:'sickNoteStartDates' or parameters.key:'sickPeriodDates') and channel:'ESERVICE_INTERNAL'";
		// Fetch errands that need migration
		final var errandsToMigrate = supportManagementClient.findErrands(municipalityId, namespace, filter);

		errandsToMigrate.getContent().forEach(errand -> {
			final var errandId = errand.getId();
			var existingParameters = errand.getParameters();
			if (existingParameters == null) {
				return;
			}
			// Create new parameters without the sickPeriod and sickNote
			var filteredParameters = existingParameters.stream()
				.filter(parameter -> !KEY_SICK_PERIOD_DATES.equals(parameter.getKey()) &&
					!KEY_SICK_PERIOD_START_TIMES.equals(parameter.getKey()) &&
					!KEY_SICK_PERIOD_END_TIMES.equals(parameter.getKey()) &&
					!KEY_SICK_NOTES.equals(parameter.getKey()) &&
					!KEY_SICK_NOTE_PERCENTAGES.equals(parameter.getKey()) &&
					!KEY_SICK_NOTE_START_DATES.equals(parameter.getKey()) &&
					!KEY_SICK_NOTE_END_DATES.equals(parameter.getKey()) &&
					!KEY_TIME_CARE.equals(parameter.getKey()) &&
					!KEY_CURRENT_SCHEDULE.equals(parameter.getKey()))
				.toList();

			var newParameters = new ArrayList<>(filteredParameters);
			// Add sickPeriod parameters
			newParameters.addAll(getSickPeriodParameters(existingParameters));

			// Add sickNote parameters
			newParameters.addAll(getSickNoteParameters(existingParameters));

			// Update errand with new parameters
			supportManagementClient.updateErrandParameters(municipalityId, namespace, errandId, newParameters);
		});
	}

	private List<Parameter> getSickPeriodParameters(final List<Parameter> existingParameters) {
		var sickPeriodDates = new ArrayList<String>();
		var sickPeriodStartTimes = new ArrayList<String>();
		var sickPeriodEndTimes = new ArrayList<String>();
		var newParameters = new ArrayList<Parameter>();

		existingParameters.stream()
			.filter(parameter -> KEY_SICK_PERIOD_DATES.equals(parameter.getKey()))
			.forEach(parameter -> sickPeriodDates.addAll(parameter.getValues()));

		existingParameters.stream()
			.filter(parameter -> KEY_SICK_PERIOD_START_TIMES.equals(parameter.getKey()))
			.forEach(parameter -> sickPeriodStartTimes.addAll(parameter.getValues()));

		existingParameters.stream()
			.filter(parameter -> KEY_SICK_PERIOD_END_TIMES.equals(parameter.getKey()))
			.forEach(parameter -> sickPeriodEndTimes.addAll(parameter.getValues()));

		if (!sickPeriodDates.isEmpty() && !sickPeriodStartTimes.isEmpty() && !sickPeriodEndTimes.isEmpty()) {
			var parameter = new Parameter().key(KEY_SICK_PERIODS).displayName(DISPLAY_SICK_PERIODS).group(GROUP_SICK_PERIOD);

			for (int i = 0; i < sickPeriodDates.size(); i++) {
				final var sickPeriodDate = sickPeriodDates.get(i);
				final var sickPeriodStartTime = sickPeriodStartTimes.size() > i ? sickPeriodStartTimes.get(i) : "";
				final var sickPeriodEndTime = sickPeriodEndTimes.size() > i ? sickPeriodEndTimes.get(i) : "";
				parameter.addValuesItem(String.format(SICK_PERIOD_ROW, Optional.ofNullable(sickPeriodDate).orElse(""),
					Optional.ofNullable(sickPeriodStartTime).orElse(""), sickPeriodEndTime));
			}
			newParameters.add(parameter);
		}

		return newParameters;
	}

	private List<Parameter> getSickNoteParameters(final List<Parameter> existingParameters) {
		var sickNotePercentages = new ArrayList<String>();
		var sickNoteStartDates = new ArrayList<String>();
		var sickNoteEndDates = new ArrayList<String>();
		var sickNoteTimeCares = new ArrayList<String>();
		var sickNoteCurrentSchedules = new ArrayList<String>();
		var newParameters = new ArrayList<Parameter>();

		existingParameters.stream()
			.filter(parameter -> KEY_SICK_NOTE_PERCENTAGES.equals(parameter.getKey()))
			.forEach(parameter -> sickNotePercentages.addAll(parameter.getValues()));
		existingParameters.stream()
			.filter(parameter -> KEY_SICK_NOTE_START_DATES.equals(parameter.getKey()))
			.forEach(parameter -> sickNoteStartDates.addAll(parameter.getValues()));
		existingParameters.stream()
			.filter(parameter -> KEY_SICK_NOTE_END_DATES.equals(parameter.getKey()))
			.forEach(parameter -> sickNoteEndDates.addAll(parameter.getValues()));
		existingParameters.stream()
			.filter(parameter -> KEY_TIME_CARE.equals(parameter.getKey()))
			.forEach(parameter -> sickNoteTimeCares.addAll(parameter.getValues()));
		existingParameters.stream()
			.filter(parameter -> KEY_CURRENT_SCHEDULE.equals(parameter.getKey()))
			.forEach(parameter -> sickNoteCurrentSchedules.addAll(parameter.getValues()));

		// Create new parameter for sickPeriod
		if (!sickNoteStartDates.isEmpty()) {
			var parameter = new Parameter().key(KEY_SICK_NOTES).displayName(DISPLAY_SICK_NOTES).group(GROUP_SICK_NOTE);

			for (int i = 0; i < sickNoteStartDates.size(); i++) {
				final var sickNoteStartDate = sickNoteStartDates.get(i);
				final var sickNoteEndDate = sickNoteEndDates.size() > i ? sickNoteEndDates.get(i) : "";
				final var sickNotePercentage = sickNotePercentages.size() > i ? sickNotePercentages.get(i) : "";
				final var sickNotTimeCare = sickNoteTimeCares.size() > i ? sickNoteTimeCares.get(i) : "";
				final var sickNoteCurrentSchedule = sickNoteCurrentSchedules.size() > i ? sickNoteCurrentSchedules.get(i) : "";
				parameter.addValuesItem(String.format(SICK_NOTE_ROW, sickNoteStartDate, sickNoteEndDate, sickNotePercentage, sickNotTimeCare, sickNoteCurrentSchedule));
			}
			newParameters.add(parameter);
		}

		return newParameters;
	}
}
