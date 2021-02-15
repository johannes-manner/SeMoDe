package de.uniba.dsg.serverless.pipeline.benchmark.provider.aws;

import com.amazonaws.services.logs.model.OutputLogEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * {@code LogAnaylzer} is an utility class for analyzing log data and a basket for all public static final string values
 * for the search and replace functionality of the test file generation based on a template in the resource part of the
 * jar file.
 *
 * @author Johannes Manner
 * @version 1.0
 */
@Slf4j
public final class AWSLogAnalyzer {

    public static final String HANDLER_CLASS = "HANDLERCLASS";
    public static final String HANDLER_METHOD = "HANDLERMETHOD";
    public static final String FILE_NAME = "FILENAME";
    public static final String INPUT_CLASS = "INPUTCLASS";
    public static final String OUTPUT_CLASS = "OUTPUTCLASS";
    public static final String FUNCTION_LOG = "FUNCTION_LOG";
    public static final String INPUT_JSON = "INPUTJSON";
    public static final String ERROR_OBJECT_VALUE = "JsonError";
    public static final String OUTPUT_JSON = "OUTPUTJSON";
    /**
     * The troubleshoot prefix is used in the logged data to identify the instrumentation messages
     */
    public static final String TROUBLESHOOT_PREFIX = "TroubleshootLambda::handleRequest";
    /**
     * Pattern is used for splitting the instrumentation messages in their parts and getting the content of these
     * messages.
     */
    public static final String TROUBLESHOOT_SPLIT_PATTERN = "::";
    private static final String EVENT_MESSAGE_START = "START";
    private static final String EVENT_MESSAGE_END = "REPORT";
    /**
     * AWS log group name is constructed like a file path with the '/' separator
     */
    private static final String AWS_GROUP_SPLIT_PATTERN = "/";
    /**
     * Number of strings in an report message (AWS Cloud Watch), when splitting with a blank.
     */
    private static final int STRING_IN_END_MESSAGE = 15;
    /**
     * Number of strings in a report message (AWS Cloud Watch), when splitting with a blank. It's the first message,
     * where the init duration is also included.
     */
    private static final int STRING_IN_END_MESSAGE_FIRST_LOG_MESSAGE = 18;

    /**
     * Generates a list of cohesive elements out of the log messages in the OutputLogEvent list. Each
     * FunctionExecutionEvent in the returned list consists of a list of OutputLogEvents which represents a single
     * function execution and is therefore in itself consistent.
     */
    public static List<FunctionExecutionEvent> generateEventList(final List<OutputLogEvent> logEvents, final String logGroupName, final String logStream) {

        final List<FunctionExecutionEvent> groupedEvents = new ArrayList<>();
        final String functionName = extractFunctionName(logGroupName);
        FunctionExecutionEvent cohesiveEvent = null;
        String message = null;

        for (final OutputLogEvent event : logEvents) {
            message = event.getMessage();
            if (message.startsWith(EVENT_MESSAGE_START)) {
                final String requestId = AWSLogAnalyzer.extractRequestId(message);
                cohesiveEvent = new FunctionExecutionEvent(functionName, logStream, requestId);
            }
            cohesiveEvent.addLogEvent(event);
            if (message.startsWith(EVENT_MESSAGE_END)) {
                groupedEvents.add(cohesiveEvent);
            }
        }

        return groupedEvents;
    }

    /**
     * Extracts the function name from the log group name. The function name is the last element in the log group name.
     */
    private static String extractFunctionName(final String logGroupName) {
        String functionName = "default";
        final String[] logGroupNameSplit = logGroupName.split(AWS_GROUP_SPLIT_PATTERN);
        if (logGroupNameSplit.length > 1) {
            functionName = logGroupNameSplit[logGroupNameSplit.length - 1];
        }
        return functionName;
    }

    /**
     * This method extracts the request id to generate unique files without a naming collision.</br> An example for a
     * start message is:</br> {@code "START RequestId: 8999e97a-f879-11e7-b329-35e05c633669 Version: $LATEST"}</br>
     *
     * @param message the start message of the log data
     * @return the altered request id
     * @throws IllegalArgumentException, if the message structure of the start message is altered.
     */
    private static String extractRequestId(final String message) {
        final String[] elements = message.split(" ");
        if (elements.length == 5) {
            return elements[2];
        } else {
            throw new IllegalArgumentException("Start message corrupted: " + message + " An investigation is needed");
        }
    }

    /**
     * Transforms the JSON, because the ObjectMapper#read-methods interpret the string as string and " indicates start
     * or end of a message. When transforming " into \" the json reader interprets the json right.
     */
    private static String transformIntoJsonString(final String json) {
        String jsonResult = json.replaceAll("\"", Matcher.quoteReplacement("\\\""));
        jsonResult = "\"" + jsonResult + "\"";
        return jsonResult;
    }

    public static PerformanceData extractInformation(final FunctionExecutionEvent event) {

        if (event == null) {
            return new PerformanceData();
        }

        String[] messageParts = null;
        long startTime = -1;

        for (final OutputLogEvent logEvent : event.getEvents()) {
            if (logEvent.getMessage().startsWith(AWSLogAnalyzer.EVENT_MESSAGE_START)) {
                startTime = logEvent.getTimestamp();
            }
            if (logEvent.getMessage().startsWith(AWSLogAnalyzer.EVENT_MESSAGE_END)) {
                messageParts = logEvent.getMessage().split(" ");
                break;
            }
        }

        if (messageParts == null) {
            log.warn("The investigated log event does not contain any messages");
            return new PerformanceData();
        } else if (messageParts.length != STRING_IN_END_MESSAGE && messageParts.length != STRING_IN_END_MESSAGE_FIRST_LOG_MESSAGE) {
            log.warn("The investigated log event does not contain an end message with performance data.");
            String errorMessage = "Size of the report message: " + messageParts.length + " Split of the report message : ";
            for (int i = 0; i < messageParts.length; i++) {
                errorMessage += i + " " + messageParts[i] + ", ";
            }
            log.info(errorMessage);
            return new PerformanceData();
        }

        final LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());

        final double preciseDuration = Double.parseDouble(messageParts[3]);
        final int billedDuration = Integer.parseInt(messageParts[6]);
        final int memorySize = Integer.parseInt(messageParts[9]);
        final int memoryUsed = Integer.parseInt(messageParts[13]);

        return new PerformanceData(event.getFunctionName(),
                event.getLogStream(),
                event.getRequestId(),
                time,
                time.plusNanos((long) (preciseDuration * 1_000_000)),
                0,
                preciseDuration,
                billedDuration,
                memorySize,
                memoryUsed);
    }
}
