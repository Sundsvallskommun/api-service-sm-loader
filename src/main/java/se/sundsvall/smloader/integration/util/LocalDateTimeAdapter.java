package se.sundsvall.smloader.integration.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@ExcludeFromJacocoGeneratedCoverageReport
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	@Override
	public LocalDateTime unmarshal(final String s) {
		if (s == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE_TIME.parse(s, LocalDateTime::from);
	}

	@Override
	public String marshal(final LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE_TIME.format(localDateTime.truncatedTo(ChronoUnit.MICROS));
	}
}
