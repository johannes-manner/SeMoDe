package de.uniba.dsg.serverless.azure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzureLogAnalyzer {

	public static final String REQUEST_ID_REGEX = "Id=[0-9a-f\\-]+";
	public static final String DURATION_REGEX = "Duration=[0-9]+";

	/**
	 * This method extracts the request id to generate unique files without a naming
	 * collision.</br>
	 * 
	 * @param logMessage
	 * @throws IllegalArgumentException,
	 *             if the message structure does not contain an id as defined in the
	 *             regular expression {@link #REQUEST_ID_REGEX}
	 * @return
	 */
	public static String extractRequestId(String logMessage) {
		Pattern p = Pattern.compile(REQUEST_ID_REGEX);
		Matcher matcher = p.matcher(logMessage);
		if (matcher.find()) {
			// Cut off Id=
			return matcher.group().substring(3);
		} else {
			throw new IllegalArgumentException("Log Message Corrupted.");
		}
	}

	/**
	 * This method extracts the duration from the log message.</br>
	 * 
	 * @param logMessage
	 * @throws IllegalArgumentException,
	 *             if the message structure does not contain a duration as defined
	 *             in the regular expression {@link #DURATION_REGEX}
	 * @return
	 */
	public static double extractDuration(String logMessage) {
		Pattern p = Pattern.compile(DURATION_REGEX);
		Matcher matcher = p.matcher(logMessage);
		if (matcher.find()) {
			// Cut off Duration=
			String duration = matcher.group().substring("Duration=".length());
			return Double.valueOf(duration);
		} else {
			throw new IllegalArgumentException("Log Message Corrupted.");
		}
	}

	public static LocalDateTime parseTime(String logTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		return LocalDateTime.parse(logTime, formatter);
	}

}
