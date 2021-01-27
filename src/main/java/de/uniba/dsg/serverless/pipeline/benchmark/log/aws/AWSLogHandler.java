package de.uniba.dsg.serverless.pipeline.benchmark.log.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.*;
import de.uniba.dsg.serverless.pipeline.benchmark.log.LogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class starts the analysis of the log streams of the specified log group. It introduces a grouping of single log
 * message in AmazonCloudWatch to cohesive log events, which expresses a single function execution.
 *
 * @author Johannes Manner
 * @version 1.0
 */
@Slf4j
public final class AWSLogHandler implements LogHandler {

    private final String region;
    private final String logGroupName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private final AWSLogs amazonCloudLogs;

    /**
     * Constructs an object with the three configuration parameters region, log group name and search string.
     *
     * @param region       - the AWS region as a string representation, e.g. "eu-west-1" for Ireland
     * @param logGroupName - the complete log group name
     */
    public AWSLogHandler(final String region, final String logGroupName, final LocalDateTime startTime, final LocalDateTime endTime) {

        this.region = region;
        this.logGroupName = logGroupName;
        this.startTime = startTime;
        this.endTime = endTime;

        this.amazonCloudLogs = this.buildAmazonCloudLogsClient();
    }

    /**
     * Builds a client for accessing the log data
     */
    private AWSLogs buildAmazonCloudLogsClient() {
        final AWSLogsClientBuilder logsBuilder = AWSLogsClientBuilder.standard();
        logsBuilder.setRegion(this.region);
        final AWSLogs logs = logsBuilder.build();
        return logs;
    }

    private List<FunctionExecutionEvent> prepareLogEventList() throws SeMoDeException {

        final List<FunctionExecutionEvent> logEventList = new ArrayList<>();

        for (final LogStream logStream : this.getLogStreams()) {
            log.info("Investigated LogStream " + logStream.getArn());
            final List<OutputLogEvent> logEvents = this.getOutputLogEvent(logStream.getLogStreamName());

            final List<FunctionExecutionEvent> logStreamEvents = AWSLogAnalyzer.generateEventList(logEvents, this.logGroupName,
                    logStream.getArn());

            logEventList.addAll(logStreamEvents);
        }

        return logEventList;
    }

    /**
     * This method computes function execution events out of the plain text messages from the logging service.
     *
     * @return a list of function execution events
     */
    public List<FunctionExecutionEvent> getLogEventList() throws SeMoDeException {
        return this.prepareLogEventList();
    }

    /**
     * This method returns a list of log streams based on the logGroupName attribute of the object.
     * <p/>
     * There is a problem with the API of cloudwatch. The limit for retrieving log stream is 50 for a single
     * DescribeLogStreamsResult. Therefore, the function has to specify
     *
     * @return List of {@link LogStream}
     */
    private List<LogStream> getLogStreams() throws SeMoDeException {
        try {
            DescribeLogStreamsRequest logRequest = new DescribeLogStreamsRequest(this.logGroupName);
            DescribeLogStreamsResult logResponse = this.amazonCloudLogs.describeLogStreams(logRequest);
            final List<LogStream> streams = logResponse.getLogStreams();

            boolean furtherLogRequest = true;

            do {
                logRequest = new DescribeLogStreamsRequest(this.logGroupName);
                logRequest.setNextToken(logResponse.getNextToken());

                logResponse = this.amazonCloudLogs.describeLogStreams(logRequest);

                final List<LogStream> responseStreams = logResponse.getLogStreams();

                if (responseStreams.isEmpty()) {
                    furtherLogRequest = false;
                    // Check the first returned log stream to avoid duplicates in the returned list
                } else if (streams.contains(responseStreams.get(0))) {
                    furtherLogRequest = false;
                } else {
                    streams.addAll(logResponse.getLogStreams());
                }
            } while (furtherLogRequest);//logResponse.getLogStreams().size() > 0);

            log.info("Number of Log streams: " + streams.size());

            return this.filterLogStreams(streams);
        } catch (final ResourceNotFoundException e) {
            throw new SeMoDeException("Resource not found. Please check the deployment of the specified function and the corresponding region!", e);
        } catch (final SdkClientException e) {
            throw new SeMoDeException("A SDK client exception occurred. Open an issue on Github", e);
        }
    }

    /**
     * Filters the log streams, based on the start and end time specified in this class.
     */
    private List<LogStream> filterLogStreams(final List<LogStream> logStreams) {

        final long startMillis = this.startTime.minusDays(1).atZone(ZoneOffset.UTC.normalized()).toInstant().toEpochMilli();
        final long endMillis = this.endTime.plusDays(1).atZone(ZoneOffset.UTC.normalized()).toInstant().toEpochMilli();

        final Predicate<LogStream> startEndFilter = (LogStream stream) -> {
            if (stream.getLastIngestionTime() == null || stream.getFirstEventTimestamp() == null) {
                return false;
            }
            return stream.getLastIngestionTime() - startMillis >= 0 && endMillis - stream.getFirstEventTimestamp() >= 0;
        };

        return logStreams.stream().filter(startEndFilter).collect(Collectors.toList());
    }

    /**
     * This class generates a list of output log events based on the logGroupName attribute of the object and the given
     * logStreamName. The return value is a list of individual events, which consists of the ingestion time the message
     * etc. Important to mention is, that the list contains several cohesive events generated by a function execution.
     * </br>
     * </br>
     * An example illustrates this problem: 2 function executions generate log events. Assuming, that each execution
     * generates 12 log statements, the returned list contains 24 elements, which are ordered but not grouped to the
     * function executions.
     *
     * @param logStreamName - Name of the logStream
     * @return List of {@link OutputLogEvent}
     * @see {@link AWSLogHandler#getLogStreams()}
     * @see {@link AWSLogHandler#getLogEventList()}
     */
    private List<OutputLogEvent> getOutputLogEvent(final String logStreamName) {
        final GetLogEventsRequest logsRequest = new GetLogEventsRequest(this.logGroupName, logStreamName);
        final GetLogEventsResult logEvents = this.amazonCloudLogs.getLogEvents(logsRequest);
        return logEvents.getEvents();
    }

    @Override
    public List<PerformanceData> getPerformanceData() throws SeMoDeException {
        final List<PerformanceData> performanceData = new ArrayList<>();

        final List<FunctionExecutionEvent> eventList = this.getLogEventList();
        for (final FunctionExecutionEvent e : eventList) {
            performanceData.add(AWSLogAnalyzer.extractInformation(e));
        }

        return performanceData;
    }
}
