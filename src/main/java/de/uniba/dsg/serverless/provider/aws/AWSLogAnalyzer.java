package de.uniba.dsg.serverless.provider.aws;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.logs.model.OutputLogEvent;

import de.uniba.dsg.serverless.model.FunctionExecutionEvent;
import de.uniba.dsg.serverless.model.FunctionInstrumentation;
import de.uniba.dsg.serverless.model.PerformanceData;

/**
 * {@code LogAnaylzer} is an utility class for analyzing log data and a basket
 * for all public static final string values for the search and replace
 * functionality of the test file generation based on a template in the resource
 * part of the jar file.
 * 
 * @author Johannes Manner
 *
 * @version 1.0
 */
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

	private static final String EVENT_MESSAGE_START = "START";
	private static final String EVENT_MESSAGE_END = "REPORT";
	
	private static final Logger logger = LogManager.getLogger(AWSLogAnalyzer.class.getName());

	/**
	 * The troubleshoot prefix is used in the logged data to identify the
	 * instrumentation messages
	 */
	public static final String TROUBLESHOOT_PREFIX = "TroubleshootLambda::handleRequest";
	/**
	 * Pattern is used for splitting the instrumentation messages in their parts and
	 * getting the content of these messages.
	 */
	public static final String TROUBLESHOOT_SPLIT_PATTERN = "::";
	/**
	 * AWS log group name is constructed like a file path with the '/' separator
	 */
	private static final String AWS_GROUP_SPLIT_PATTERN = "/";
	/**
	 * Number of strings in an report message (AWS Cloud Watch), when splitting
	 * with a blank.
	 */
	private static final int STRING_IN_END_MESSAGE = 16;

	/**
	 * Generates a list of cohesive elements out of the log messages in the
	 * OutputLogEvent list. Each FunctionExecutionEvent in the returned list
	 * consists of a list of OutputLogEvents which represents a single function
	 * execution and is therefore in itself consistent.
	 * 
	 * @param logEvents
	 * @param logGroupName
	 * 
	 * @return
	 */
	public static List<FunctionExecutionEvent> generateEventList(List<OutputLogEvent> logEvents, String logGroupName, String logStream) {

		List<FunctionExecutionEvent> groupedEvents = new ArrayList<>();
		String functionName = extractFunctionName(logGroupName);
		FunctionExecutionEvent cohesiveEvent = null;
		String message = null;

		for (OutputLogEvent event : logEvents) {
			message = event.getMessage();
			if (message.startsWith(EVENT_MESSAGE_START)) {
				String requestId = AWSLogAnalyzer.extractRequestId(message);
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
	 * Extracts the function name from the log group name. The function name is the
	 * last element in the log group name.
	 * 
	 * @param logGroupName
	 * @return
	 */
	private static String extractFunctionName(String logGroupName) {
		String functionName = "default";
		String[] logGroupNameSplit = logGroupName.split(AWS_GROUP_SPLIT_PATTERN);
		if (logGroupNameSplit.length > 1) {
			functionName = logGroupNameSplit[logGroupNameSplit.length - 1];
		}
		return functionName;
	}

	/**
	 * This method extracts the request id to generate unique files without a naming
	 * collision.</br>
	 * An example for a start message is:</br>
	 * {@code "START RequestId: 8999e97a-f879-11e7-b329-35e05c633669 Version: $LATEST"}</br>
	 * 
	 * @param message
	 *            the start message of the log data
	 * 
	 * @throws IllegalArgumentException,
	 *             if the message structure of the start message is altered.
	 * 
	 * @return the altered request id
	 */
	private static String extractRequestId(String message) {
		String[] elements = message.split(" ");
		if (elements.length == 5) {
			return elements[2];
		} else {
			throw new IllegalArgumentException("Start message corrupted: " + message + " An investigation is needed");
		}
	}

	/**
	 * Generates, based on a single consistent function execution log object, an
	 * instrumentation object for further processing. In this function, the
	 * extraction of the information is made based on a string comparison of
	 * different log message, which are all starting with the troubleshooting
	 * pattern "TroubleshootLambda::handleRequest".
	 * 
	 * @param logEvent
	 * 
	 * @return
	 */
	public static FunctionInstrumentation generateFunctionInstrumentation(FunctionExecutionEvent logEvent) {
		String handlerClass = "";
		String handlerMethod = "";
		String inputClass = "";
		String jsonInput = "";
		String outputClass = "";
		String jsonOutput = "";

		for (OutputLogEvent event : logEvent.getEvents()) {
			if (event.getMessage().startsWith(TROUBLESHOOT_PREFIX)) {
				String[] splitResult = event.getMessage().split(TROUBLESHOOT_SPLIT_PATTERN);
				if (splitResult.length == 4) {
					String key = splitResult[2];
					String value = splitResult[3];

					if (key.equals(HANDLER_CLASS)) {
						handlerClass = value;
					} else if (key.equals(HANDLER_METHOD)) {
						handlerMethod = value;
					} else if (key.equals(INPUT_CLASS)) {
						inputClass = value;
					} else if (key.equals(INPUT_JSON)) {
						jsonInput = transformIntoJsonString(value);
					} else if (key.equals(OUTPUT_CLASS)) {
						outputClass = value;
					} else if (key.equals(OUTPUT_JSON)) {
						jsonOutput = value;
					} else {
						throw new IllegalArgumentException(
								"Instrumentation data is corrupted: Message - " + event.getMessage());
					}

				} else {
					throw new IllegalArgumentException(
							"Instrumentation data is corrupted: Message - " + event.getMessage());
				}
			}
		}
		return new FunctionInstrumentation(handlerClass, handlerMethod, inputClass, jsonInput, outputClass, jsonOutput,
				logEvent);
	}

	/**
	 * Transforms the JSON, because the ObjectMapper#read-methods interpret the
	 * string as string and " indicates start or end of a message. When transforming
	 * " into \" the json reader interprets the json right.
	 * 
	 * @param json
	 * 
	 * @return
	 */
	private static String transformIntoJsonString(String json) {
		String jsonResult = json.replaceAll("\"", Matcher.quoteReplacement("\\\""));
		jsonResult = "\"" + jsonResult + "\"";
		return jsonResult;
	}
	
	
	public static PerformanceData extractInformation(FunctionExecutionEvent event) {
		
		if(event == null) {
			return new PerformanceData();
		}
		
		String[] messageParts = null;
		long startTime = -1;
		
		for(OutputLogEvent logEvent : event.getEvents()) {
			if(logEvent.getMessage().startsWith(AWSLogAnalyzer.EVENT_MESSAGE_START)) {
				startTime = logEvent.getTimestamp();
			}
			if(logEvent.getMessage().startsWith(AWSLogAnalyzer.EVENT_MESSAGE_END)){
				messageParts = logEvent.getMessage().split(" ");
				break;
			}
		}
		
		if(messageParts == null || messageParts.length != STRING_IN_END_MESSAGE) {
			logger.fatal("The investigated log event does not contain an end message with performance data.");
			logger.info("Split of the report message : " + Arrays.toString(messageParts));
			return new PerformanceData();
		}
		
		LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
		
		double preciseDuration = Double.parseDouble(messageParts[3]);
		int billedDuration = Integer.parseInt(messageParts[6]);
		int memorySize = Integer.parseInt(messageParts[10]);
		int memoryUsed = Integer.parseInt(messageParts[14]);
		
		return new PerformanceData(event.getFunctionName(), 
				event.getLogStream(),
				event.getRequestId(), 
				time, 
				time.plusNanos((long)(preciseDuration*1_000_000)),
				0, 
				preciseDuration, 
				billedDuration,
				memorySize,
				memoryUsed);
	}
}
