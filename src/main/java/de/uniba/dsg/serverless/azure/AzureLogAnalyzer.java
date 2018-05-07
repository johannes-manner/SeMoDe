package de.uniba.dsg.serverless.azure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AzureLogAnalyzer {

	/**
	 * This method parses the time provided in the format "yyyy-MM-dd'T'HH:mm:ss.SSS"
	 * @param logTime time
	 * @return parsed local date time
	 */
	public static LocalDateTime parseTime(String logTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.][SSS][SS][S]'Z'");
		return LocalDateTime.parse(logTime, formatter);
	}

}
