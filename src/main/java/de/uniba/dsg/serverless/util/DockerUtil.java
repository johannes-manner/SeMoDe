package de.uniba.dsg.serverless.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DockerUtil {

    /**
     * Parses the time as ISO_DATE_TIME
     *
     * @param time Time to be formatted as an ISO_DATE_TIME (e.g. 2019-01-01T10:10:30.1337Z)
     * @return time as long
     * @throws SeMoDeException when the time is not of ISO_DATE_TIME format
     */
    public static long parseTime(final String time) throws SeMoDeException {
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME).toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
        } catch (final DateTimeParseException e) {
            throw new SeMoDeException("Date does not have the correct format. time:" + time);
        }
    }


}
