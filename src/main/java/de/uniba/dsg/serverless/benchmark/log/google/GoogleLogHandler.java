package de.uniba.dsg.serverless.benchmark.log.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.Payload.StringPayload;
import de.uniba.dsg.serverless.benchmark.log.LogHandler;
import de.uniba.dsg.serverless.model.PerformanceData;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Provide your Credentials to use the GoogleLogHandler via the Google Cloud SDK
 * command <i>gcloud auth application-default login</i>.
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

    public GoogleLogHandler(final String functionName, final LocalDateTime startTime, final LocalDateTime endTime) {
        this.functionName = functionName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private List<LogEntry> extractLogEntryFromPage(final Page<LogEntry> entries) {
        final List<LogEntry> logEntries = new ArrayList<>();
        for (final LogEntry entry : entries.iterateAll()) {
            logEntries.add(entry);
        }
        return logEntries;
    }

    /**
     * Executes an API call throw the Google Functions SDK to get the log entries.
     * A custom filter is applied with the function name and the start and end time, where
     * logs should be fetched.
     * <p>
     * Consider the UTC time.
     */
    private List<LogEntry> executeListLogEntries() throws SeMoDeException {

        final List<LogEntry> logEntries = new ArrayList<>();

        // project is inferred from the environment
        final LoggingOptions options = LoggingOptions.getDefaultInstance();

        try (final Logging logging = options.getService()) {

            final String filter = "resource.type=\"cloud_function\" AND " + "resource.labels.function_name = \""
                    + this.functionName + "\" AND " + "timestamp > \"" + this.startTime.format(QUERY_DATE_FORMATTER)
                    + "\" AND " + "timestamp < \"" + this.endTime.format(QUERY_DATE_FORMATTER) + "\"";

            final EntryListOption entryFilterOption = EntryListOption.filter(filter);

            final Page<LogEntry> entries = logging.listLogEntries(entryFilterOption);
            logEntries.addAll(this.extractLogEntryFromPage(entries));

            return logEntries;
        } catch (final Exception e) {
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

        final List<LogEntry> logEntries = this.executeListLogEntries();

        final Map<String, List<LogEntry>> eventMap = this.generateEventMapByExecutionId(logEntries);

        return this.generatePerformanceDataMap(eventMap);
    }

    private Map<String, WritableEvent> generatePerformanceDataMap(final Map<String, List<LogEntry>> eventMap) throws SeMoDeException {

        final Map<String, WritableEvent> performanceMap = new HashMap<>();

        for (final String executionId : eventMap.keySet()) {
            final List<LogEntry> cohesiveEvent = eventMap.get(executionId);
            final Optional<PerformanceData> data = this.extractPerformanceData(cohesiveEvent);
            if (data.isPresent()) {
                performanceMap.put(data.get().getRequestId(), data.get());
            }
        }

        return performanceMap;
    }

    /**
     * Extracts the relevant information from a list of log entries, which represent a cohesive event.
     * A PerformanceData object is created from the extracted information and returned.
     */
    private Optional<PerformanceData> extractPerformanceData(final List<LogEntry> cohesiveEvent) throws SeMoDeException {

        LocalDateTime startTime = LocalDateTime.MIN;
        LocalDateTime endTime = LocalDateTime.MIN;
        double preciseDuration = -1.0;
        String platformId = "";
        String containerId = "";

        for (final LogEntry entry : cohesiveEvent) {
            final Payload payload = entry.getPayload();
            if (payload instanceof StringPayload) {
                final String data = ((StringPayload) payload).getData();
                if (data.startsWith(GOOGLE_FUNCTION_EXECUTION_SUMMARY)) {
                    preciseDuration = Double.parseDouble(data.split(GOOGLE_FUNCTION_EXECUTION_SUMMARY_LOG_REGEX)[3]);
                    endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getTimestamp()), ZoneId.systemDefault());
                } else if (data.startsWith(GOOGLE_FUNCTION_EXECUTION_START)) {
                    startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getTimestamp()), ZoneId.systemDefault());
                } else if (data.startsWith(SEMODE_CUSTOM_LOG_PREFIX)) {
                    final ObjectMapper om = new ObjectMapper();
                    try {
                        final JsonNode node = om.readTree(data.split(SEMODE_CUSTOM_LOG_REGEX)[1]);
                        platformId = this.extractStringValue(node, "platformId");
                        containerId = this.extractStringValue(node, "containerId");
                    } catch (final IOException e) {
                        throw new SeMoDeException("Exception while reading the custom json message from google API", e);
                    }
                }
            } else {
                // payload can also be something different than string.
                // these payloads are not relevant for the handling functionality.
                return Optional.empty();
            }

        }

        final PerformanceData data = new PerformanceData(this.functionName,
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
    private Map<String, List<LogEntry>> generateEventMapByExecutionId(final List<LogEntry> logEntries) {
        final Map<String, List<LogEntry>> eventMap = new HashMap<>();

        for (final LogEntry logEntry : logEntries) {
            final String executionId = logEntry.getLabels().get("execution_id");
            if (!eventMap.containsKey(executionId)) {
                eventMap.put(executionId, new ArrayList<>());
            }
            eventMap.get(executionId).add(logEntry);
        }

        return eventMap;
    }

    private String extractStringValue(final JsonNode node, final String key) {
        if (node.has(key)) {
            return node.get(key).asText();
        }
        return "";
    }
}
