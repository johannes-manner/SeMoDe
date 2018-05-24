package de.uniba.dsg.serverless.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.azure.AzureLogHandler;

public final class AzurePerformanceDataUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(AzurePerformanceDataUtility.class.getName());

	private static final String DATETIME_FORMAT_PATTERN =  "\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2}";

	public AzurePerformanceDataUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 5) {
			logger.fatal("Wrong parameter size: "
					+ "\n(1) Application ID " 
					+ "\n(2) API Key " 
					+ "\n(3) Function Name"
					+ "\n(4) Start time filter of performance data"
					+ "\n(5) End time filter of performance data");
			return;
		}
		
		String applicationID = args.get(0);
		String apiKey = args.get(1);
		String functionName = args.get(2);
		String startTimeString = args.get(3);
		String endTimeString = args.get(4);
		
		if (!startTimeString.matches(DATETIME_FORMAT_PATTERN)) {
			logger.fatal("Start time is no valid datetime with the format: yyyy-MM-dd_HH:mm");
			return;
		}
		if (!endTimeString.matches(DATETIME_FORMAT_PATTERN)) {
			logger.fatal("End time is no valid datetime with the format: yyyy-MM-dd_HH:mm");
			return;
		}
		
		LocalDateTime startTime = parseTime(startTimeString);
		LocalDateTime endTime = parseTime(endTimeString);

		String dateText = new SimpleDateFormat("MM-dd-HH-mm").format(new Date());
		String fileName = functionName + "-" + dateText + ".csv";

		new AzureLogHandler(applicationID, apiKey, functionName, startTime, endTime)
				.writePerformanceDataToFile(fileName);
	}
	
	/**
	 * Parses the start and end time parameter to a LocalDateTime.
	 * 
	 * @param time The time as string.
	 * @return The time as LocalDateTime.
	 */
	private static LocalDateTime parseTime(String time) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'_'HH:mm");
		return LocalDateTime.parse(time, formatter);
	}
	
}
