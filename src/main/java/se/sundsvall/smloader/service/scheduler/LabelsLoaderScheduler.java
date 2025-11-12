package se.sundsvall.smloader.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.smloader.integration.util.LabelsProvider;

@Service
public class LabelsLoaderScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabelsLoaderScheduler.class);
	private static final String LOG_LOADING_LABELS_STARTED = "Beginning of loading labels";
	private static final String LOG_LOADING_LABELS_ENDED = "Loading labels has ended";

	private final LabelsProvider labelsProvider;

	public LabelsLoaderScheduler(final LabelsProvider labelsProvider) {

		this.labelsProvider = labelsProvider;
	}

	@Dept44Scheduled(
		cron = "${scheduler.labelsloader.cron}",
		name = "${scheduler.labelsloader.name}",
		lockAtMostFor = "${scheduler.labelsloader.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.labelsloader.maximum-execution-time}")
	public void execute() {
		LOGGER.info(LOG_LOADING_LABELS_STARTED);
		labelsProvider.refresh();
		LOGGER.info(LOG_LOADING_LABELS_ENDED);
	}
}
