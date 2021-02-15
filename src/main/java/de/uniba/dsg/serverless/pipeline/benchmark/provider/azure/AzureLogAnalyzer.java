package de.uniba.dsg.serverless.pipeline.benchmark.provider.azure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated(since = "January 2021")
public class AzureLogAnalyzer {

    private static final String HOST_STARTED_DURATION_REGEX = "Host started \\(([0-9]+)ms\\)";

    /**
     * This method parses the time provided in the format "yyyy-MM-dd'T'HH:mm:ss.SSS"
     *
     * @param logTime time
     * @return parsed local date time
     */
    public static LocalDateTime parseTime(final String logTime) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.][SSSSSSSS][SSSSSSS][SSSSSS][SSSSS][SSSS][SSS][SS][S]'Z'");
        return LocalDateTime.parse(logTime, formatter);
    }

    /**
     * Parses the log message of a started host and returns the host startup duration.
     *
     * @param logMessage The log message of a started host (starting with "Host started").
     * @return The host startup duration extracted from the log message.
     */
    public static double parseHostStartupDuration(final String logMessage) {
        final Pattern p = Pattern.compile(HOST_STARTED_DURATION_REGEX);
        final Matcher matcher = p.matcher(logMessage);
        if (matcher.find()) {
            final String duration = matcher.group(1);
            return Double.parseDouble(duration);
        } else {
            throw new IllegalArgumentException("Log Message Corrupted. No host startup duration found.");
        }
    }
}
