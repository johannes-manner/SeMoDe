package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.util.SeMoDeException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class CustomUtility {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'_'HH:mm";
    private static final String DATETIME_FORMAT_PATTERN = "\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2}";

    private final String name;

    public CustomUtility(final String name) {
        this.name = name;
    }

    public abstract void start(List<String> args);

    public String getName() {
        return this.name;
    }

    /**
     * Parses the start and end time parameter to a LocalDateTime.
     *
     * @param time The time as string.
     * @return The time as LocalDateTime.
     */
    protected LocalDateTime parseTime(final String time) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        return LocalDateTime.parse(time, formatter);
    }

    protected void validateStartEnd(final String startTime, final String endTime) throws SeMoDeException {
        if (!startTime.matches(DATETIME_FORMAT_PATTERN)) {
            throw new SeMoDeException("Start time is no valid datetime with the format: " + DATE_TIME_PATTERN);
        }
        if (!endTime.matches(DATETIME_FORMAT_PATTERN)) {
            throw new SeMoDeException("End time is no valid datetime with the format: " + DATE_TIME_PATTERN);
        }
    }
}
