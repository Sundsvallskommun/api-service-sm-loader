package se.sundsvall.smloader.integration.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ExcludeFromJacocoGeneratedCoverageReport
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(final String s) {
        if (s == null) {
            return null;
        }

        return DateTimeFormatter.ISO_DATE.parse(s, LocalDate::from);
    }

    @Override
    public String marshal(final LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return DateTimeFormatter.ISO_DATE.format(localDate);
    }
}
