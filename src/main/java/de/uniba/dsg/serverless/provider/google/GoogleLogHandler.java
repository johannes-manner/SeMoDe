package de.uniba.dsg.serverless.provider.google;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.Payload.StringPayload;

import de.uniba.dsg.serverless.model.PerformanceData;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;
import de.uniba.dsg.serverless.provider.LogHandler;

/**
 * Provide your Credentials to use the GoogleLogHandler via the Google Cloud SDK
 * command <i>gcloud auth application-default login</i>.
 * 
 */
public class GoogleLogHandler implements LogHandler {

	private static final String GOOGLE_FUNCTION_EXECUTION_SUMMARY_LOG_REGEX = " ";

	private static final String SEMODE_CUSTOM_LOG_REGEX = "::";

	private static final String SEMODE_CUSTOM_LOG_PREFIX = "SEMODE::";

	private static final String GOOGLE_FUNCTION_EXECUTION_START = "Function execution start";

	private static final String GOOGLE_FUNCTION_EXECUTION_SUMMARY = "Function execution took";

	private static final DateTimeFormatter QUERY_DATE_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private final String functionName;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;

	public GoogleLogHandler(String functionName, LocalDateTime startTime, LocalDateTime endTime) {
		this.functionName = functionName;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	private List<LogEntry> extractLogEntryFromPage(Page<LogEntry> entries) {
		List<LogEntry> logEntries = new ArrayList<>();
		for (LogEntry entry : entries.iterateAll()) {
			logEntries.add(entry);
		}
		return logEntries;
	}

	/**
	 * Executes an API call throw the Google Functions SDK to get the log entries.
	 * A custom filter is applied with the function name and the start and end time, where
	 * logs should be fetched.
	 * 
	 * Consider the UTC time.
	 */
	private List<LogEntry> executeListLogEntries() throws SeMoDeException {

		List<LogEntry> logEntries = new ArrayList<>();

		// project is inferred from the environment
		LoggingOptions options = LoggingOptions.getDefaultInstance();

		try (Logging logging = options.getService()) {

			String filter = "resource.type=\"cloud_function\" AND " + "resource.labels.function_name = \""
					+ this.functionName + "\" AND " + "timestamp > \"" + this.startTime.format(QUERY_DATE_FORMATTER)
					+ "\" AND " + "timestamp < \"" + this.endTime.format(QUERY_DATE_FORMATTER) + "\"";

			EntryListOption entryFilterOption = EntryListOption.filter(filter);

			Page<LogEntry> entries = logging.listLogEntries(entryFilterOption);
			logEntries.addAll(this.extractLogEntryFromPage(entries));

			return logEntries;
		} catch (Exception e) {
			throw new SeMoDeException("An error occured during accessing the stackdriver API", e);
		}
	}

	/**
	 * Different as on AWS and Azure. The key in the returned map is typically the
	 * platform execution id. At google's platform, this key is not accessible and
	 * therefore a self-assigned key within the cloud function in form of a uuid is
	 * used to achieve the mapping from platform executions and local REST calls.
	 */
	@Override
	public Map<String, WritableEvent> getPerformanceData() throws SeMoDeException {

		List<LogEntry> logEntries = this.executeListLogEntries();

		Map<String, List<LogEntry>> eventMap = this.generateEventMapByExecutionId(logEntries);

		return this.generatePerformanceDataMap(eventMap);
	}

	private Map<String, WritableEvent> generatePerformanceDataMap(Map<String, List<LogEntry>> eventMap) throws SeMoDeException {

		Map<String, WritableEvent> performanceMap = new HashMap<>();
		
		for(String executionId : eventMap.keySet()) {
			List<LogEntry> cohesiveEvent = eventMap.get(executionId);
			Optional<PerformanceData> data = this.extractPerformanceData(cohesiveEvent);
			if(data.isPresent()) {
				performanceMap.put(data.get().getRequestId(), data.get());
			}
		}
		
		return performanceMap;
	}

	/**
	 * Extracts the relevant information from a list of log entries, which represent a cohesive event. 
	 * A PerformanceData object is created from the extracted information and returned.
	 */
	private Optional<PerformanceData> extractPerformanceData(List<LogEntry> cohesiveEvent) throws SeMoDeException {
		
		LocalDateTime startTime = LocalDateTime.MIN;
		LocalDateTime endTime = LocalDateTime.MIN;
		double preciseDuration = -1.0;
		String platformId= "";
		String containerId = "";
		
		for(LogEntry entry : cohesiveEvent) {
			Payload payload = entry.getPayload();
			if (payload instanceof StringPayload) {
				String data = ((StringPayload)payload).getData();
				if(data.startsWith(GOOGLE_FUNCTION_EXECUTION_SUMMARY)) {
					preciseDuration = Double.parseDouble(data.split(GOOGLE_FUNCTION_EXECUTION_SUMMARY_LOG_REGEX)[3]);
					endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getTimestamp()), ZoneId.systemDefault());
				}else if(data.startsWith(GOOGLE_FUNCTION_EXECUTION_START)) {
					startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getTimestamp()), ZoneId.systemDefault());
				}else if(data.startsWith(SEMODE_CUSTOM_LOG_PREFIX)) {
					ObjectMapper om = new ObjectMapper();
					try {
						JsonNode node = om.readTree(data.split(SEMODE_CUSTOM_LOG_REGEX)[1]);
						platformId = this.extractStringValue(node, "platformId");
						containerId = this.extractStringValue(node, "containerId");
					} catch (IOException e) {
						throw new SeMoDeException("Exception while reading the custom json message from google API", e);
					}
				}
			} else {
				// payload can also be something different than string.
				// these payloads are not relevant for the handling functionality.
				return Optional.empty();
			}
			
		}
		
		PerformanceData data = new PerformanceData(this.functionName,
				containerId,
				platformId,
				startTime,
				endTime,
				-1.0,
				preciseDuration,
				-1,
				-1,
				-1
				);
		return Optional.of(data);
	}

	/**
	 * Creates a map to group the log entries returned by the Stackdriver API.
	 * The key is the platform's execution_id.
	 */
	private Map<String, List<LogEntry>> generateEventMapByExecutionId(List<LogEntry> logEntries) {
		Map<String, List<LogEntry>> eventMap = new HashMap<>();

		for (LogEntry logEntry : logEntries) {
			String executionId = logEntry.getLabels().get("execution_id");
			if (!eventMap.containsKey(executionId)) {
				eventMap.put(executionId, new ArrayList<>());
			}
			eventMap.get(executionId).add(logEntry);
		}

		return eventMap;
	}
	
	private String extractStringValue(JsonNode node, String key) {
		if(node.has(key)) {
			return node.get(key).asText();
		}
		return "";
	}
}
