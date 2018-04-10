package de.uniba.dsg.serverless.semode.troubleshooting.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import com.amazonaws.services.logs.model.ResourceNotFoundException;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.GetLogEventsRequest;
import com.amazonaws.services.logs.model.GetLogEventsResult;
import com.amazonaws.services.logs.model.LogStream;
import com.amazonaws.services.logs.model.OutputLogEvent;
import com.google.common.io.Resources;

import de.uniba.dsg.serverless.semode.model.FunctionExecutionEvent;
import de.uniba.dsg.serverless.semode.model.FunctionInstrumentation;
import de.uniba.dsg.serverless.semode.model.PerformanceData;
import de.uniba.dsg.serverless.semode.model.SeMoDeException;
import de.uniba.dsg.serverless.semode.util.LogAnalyzer;

/**
 * This class starts the analysis of the log streams of the specified log group.
 * It introduces a grouping of single log message in AmazonCloudWatch to
 * cohesive log events, which expresses a single function execution.
 * 
 * 
 * @author Johannes Manner
 * 
 * @version 1.0
 *
 */
public final class AWSLogHandler {

	private static final Logger logger = Logger.getLogger(AWSLogHandler.class.getName());

	private static final String TEMPLATE_FILE = "TestTemplate.txt";
	private static final String GENERATION_PATH = "generatedTests";
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	private final String region;
	private final String logGroupName;

	private final AWSLogs amazonCloudLogs;

	/**
	 * Constructs an object with the three configuration parameters region, log
	 * group name and search string.
	 * 
	 * @param region
	 *            - the AWS region as a string representation, e.g. "eu-west-1" for
	 *            Ireland
	 * @param logGroupName
	 *            - the complete log group name
	 */
	public AWSLogHandler(String region, String logGroupName) {

		this.region = region;
		this.logGroupName = logGroupName;

		this.amazonCloudLogs = this.buildAmazonCloudLogsClient();
	}

	/**
	 * Builds a client for accessing the log data
	 * 
	 * @return
	 */
	private AWSLogs buildAmazonCloudLogsClient() {
		AWSLogsClientBuilder logsBuilder = AWSLogsClientBuilder.standard();
		logsBuilder.setRegion(this.region);
		AWSLogs logs = logsBuilder.build();
		return logs;
	}

	/**
	 * Starts the analysis based on the attributes. Logs the total number of files
	 * generated based on the input parameters. The number of investigated log
	 * streams is limited (see AWS documentation). The log streams are requested in
	 * a descending order (the newest first). So, the analysis concentrates on the
	 * latest function executions.
	 */
	public void startAnalzying(String searchString) {
		try {
			List<FunctionExecutionEvent> logEventList = this.getLogEventList(searchString);
			List<FunctionInstrumentation> jsonInstrumentationList = this.getJsonInstrumentation(logEventList);

			for (FunctionInstrumentation jsonInstrumentation : jsonInstrumentationList) {
				this.generateTestClass(jsonInstrumentation);
			}

			logger.log(Level.INFO, "Number of test files generated: " + jsonInstrumentationList.size());
		} catch (SeMoDeException e) {
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.INFO, "Prototype is terminated");
		}
	}

	/**
	 * Generates a test class.
	 * 
	 * @param instrumentation
	 * 
	 * @see AWSLogHandler#writeToTestFile
	 */
	private void generateTestClass(FunctionInstrumentation instrumentation) {

		String fileName = instrumentation.getFileName() + ".java";

		this.writeToTestFile(fileName, instrumentation.getAttributeMap());

	}

	/**
	 * Uses googles guava library to get the content of the template test file,
	 * which is included in the resources folder of the project /jar file. <br/>
	 * It creates a directory, where the generated test files are stored in, if this
	 * directory does not exist and logs a message for each generated file or when
	 * an error occured during file writing.
	 * 
	 * @param fileName
	 * @param attributeMap
	 */
	private void writeToTestFile(String fileName, Map<String, String> attributeMap) {
		Path file = Paths.get(GENERATION_PATH + File.separator + fileName);
		try {
			String content = Resources.toString(Resources.getResource(TEMPLATE_FILE), CHARSET);
			for (String key : attributeMap.keySet()) {
				content = content.replaceAll(key, Matcher.quoteReplacement(attributeMap.get(key)));
			}
			this.createDirectoryIfNotExist();
			Files.write(file, content.getBytes(CHARSET));
			logger.log(Level.INFO, "File generated: " + file);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO-Exception during file writing. file path: " + file);
		}
	}

	/**
	 * Creates a directory for the given generation path, if this directory does not
	 * exist.
	 * 
	 * @throws IOException
	 */
	private void createDirectoryIfNotExist() throws IOException {
		Path directory = Paths.get(GENERATION_PATH);
		if (!Files.exists(directory)) {
			Files.createDirectory(directory);
		}
	}

	/**
	 * Returns a list of instrumentation data for further processing. The function
	 * extracts the information from the function executions, if the search string
	 * is found in the messages of the logged data. </br>
	 * </br>
	 * This function is a prerequisite for generating test events.
	 * 
	 * @param logEventList
	 * 
	 * @return List of {@link FunctionInstrumentation}
	 */
	private List<FunctionInstrumentation> getJsonInstrumentation(List<FunctionExecutionEvent> logEventList) {

		List<FunctionInstrumentation> jsonInstrumentation = new ArrayList<>();

		for (FunctionExecutionEvent logEvent : logEventList) {
			FunctionInstrumentation instrumentation = LogAnalyzer.generateFunctionInstrumentation(logEvent);
			if (instrumentation != null) {
				jsonInstrumentation.add(instrumentation);
			}
		}
		return jsonInstrumentation;
	}

	private List<FunctionExecutionEvent> prepareLogEventList() throws SeMoDeException {

		List<FunctionExecutionEvent> logEventList = new ArrayList<>();

		for (LogStream logStream : this.getLogStreams()) {
			logger.log(Level.INFO, "Investigated LogStream " + logStream.getArn());
			List<OutputLogEvent> logEvents = this.getOutputLogEvent(logStream.getLogStreamName());

			List<FunctionExecutionEvent> logStreamEvents = LogAnalyzer.generateEventList(logEvents, this.logGroupName,
					logStream.getArn());

			logEventList.addAll(logStreamEvents);
		}

		return logEventList;
	}

	/**
	 * Generates a list of cohesive function logs. </br>
	 * </br>
	 * An example illustrates the function result: 2 function executions generate
	 * log events. Assuming, that each execution generates 12 log statements, the
	 * log event list contains 24 elements, which are ordered but not grouped to the
	 * function executions. </br>
	 * This function enables the grouping and returns the list of cohesive log data.
	 * 
	 * @return List of {@link FunctionExecutionEvent}
	 * @throws SeMoDeException
	 */
	public List<FunctionExecutionEvent> getLogEventList(String searchString) throws SeMoDeException {

		List<FunctionExecutionEvent> logEventList = new ArrayList<>();
		List<FunctionExecutionEvent> preparedEventList = this.prepareLogEventList();

		for (FunctionExecutionEvent event : preparedEventList) {
			if (event.containsSearchString(searchString)) {
				logEventList.add(event);
			}
		}
		return logEventList;
	}

	/**
	 * This method computes function execution events out of the plain text
	 * messages from the logging service.
	 * 
	 * @return a list of function execution events
	 * @throws SeMoDeException
	 */
	public List<FunctionExecutionEvent> getLogEventList() throws SeMoDeException {
		return this.prepareLogEventList();
	}

	/**
	 * This method returns a list of log streams based on the logGroupName attribute
	 * of the object. 
	 * <p/>
	 * There is a problem with the API of cloudwatch. The limit for retrieving 
	 * log stream is 50 for a single DescribeLogStreamsResult. Therefore, the function has
	 * to specify 
	 * 
	 * @return List of {@link LogStream}
	 * @throws SeMoDeException
	 */
	private List<LogStream> getLogStreams() throws SeMoDeException {
		try {
			DescribeLogStreamsRequest logRequest = new DescribeLogStreamsRequest(this.logGroupName);
			DescribeLogStreamsResult logResponse = this.amazonCloudLogs.describeLogStreams(logRequest);
			List<LogStream> streams = logResponse.getLogStreams();

			boolean furtherLogRequest = true;

			do {
				logRequest = new DescribeLogStreamsRequest(this.logGroupName);
				logRequest.setNextToken(logResponse.getNextToken());

				logResponse = this.amazonCloudLogs.describeLogStreams(logRequest);
				
				System.out.println("Number of Log streams: " + logResponse.getLogStreams().size());
				List<LogStream> responseStreams = logResponse.getLogStreams();
				
				if(responseStreams.isEmpty()) {
					furtherLogRequest = false;
				// Check the first returned log stream to avoid duplicates in the returned list
				}else if(streams.contains(responseStreams.get(0))) {
					furtherLogRequest = false;
				}else {
					streams.addAll(logResponse.getLogStreams());
				}				

			} while (furtherLogRequest);//logResponse.getLogStreams().size() > 0);

			return streams;
		} catch (ResourceNotFoundException e) {
			throw new SeMoDeException(
					"Resource not found. Please check the deployment of the specified function and the corresponding region!",
					e);
		}
	}

	/**
	 * This class generates a list of output log events based on the logGroupName
	 * attribute of the object and the given logStreamName. The return value is a
	 * list of individual events, which consists of the ingestion time the message
	 * etc. Important to mention is, that the list contains several cohesive events
	 * generated by a function execution. </br>
	 * </br>
	 * An example illustrates this problem: 2 function executions generate log
	 * events. Assuming, that each execution generates 12 log statements, the
	 * returned list contains 24 elements, which are ordered but not grouped to the
	 * function executions. See also {@link LogAnalyzer#generateEventList(List)} for
	 * grouping these elements logically to function executions.
	 * 
	 * @param logStreamName
	 *            - Name of the logStream
	 * 
	 * @return List of {@link OutputLogEvent}
	 * 
	 * @see {@link AWSLogHandler#getLogStreams()}
	 * @see {@link AWSLogHandler#getLogEventList()}
	 * @see {@link LogAnalyzer#generateEventList(List)}
	 */
	private List<OutputLogEvent> getOutputLogEvent(String logStreamName) {
		GetLogEventsRequest logsRequest = new GetLogEventsRequest(this.logGroupName, logStreamName);
		GetLogEventsResult logEvents = this.amazonCloudLogs.getLogEvents(logsRequest);
		return logEvents.getEvents();
	}

	public void writePerformanceDataToFile(String fileName) {

		List<PerformanceData> performanceDataList = new ArrayList<>();
		try {
			List<FunctionExecutionEvent> eventList = this.getLogEventList();
			for (FunctionExecutionEvent e : eventList) {
				performanceDataList.add(LogAnalyzer.extractInformation(e));
			}
			
			if(!Files.exists(Paths.get("performanceData"))){
				Files.createDirectory(Paths.get("performanceData"));
			}
			
			Path file = Files.createFile(Paths.get("performanceData/" + fileName));
			try (BufferedWriter bw = Files.newBufferedWriter(file)) {
				bw.write(PerformanceData.getCSVMetadata() + System.lineSeparator());
				for (PerformanceData performanceData : performanceDataList) {
					bw.write(performanceData.toCSVString() + System.lineSeparator());
				}
			}

		} catch (SeMoDeException e) {
			logger.severe(e.getMessage());
			logger.severe("Data handler is terminated due to an error.");
		} catch (IOException e) {
			logger.severe("IO Exception occured.");
			e.printStackTrace();
		}
	}

}
