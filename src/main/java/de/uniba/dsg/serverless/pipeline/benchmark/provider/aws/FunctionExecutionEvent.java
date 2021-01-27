package de.uniba.dsg.serverless.pipeline.benchmark.provider.aws;

import com.amazonaws.services.logs.model.OutputLogEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a model for all log and related data, like start time, end time and consumption of time and memory.
 * Because of the string based nature of log data in AmazonCloudWatch, this model class groups all data associated to a
 * single function execution to get a cohesive data structure for log events.
 *
 * @author Johannes Manner
 * @version 1.0
 */
public final class FunctionExecutionEvent {

    private final List<OutputLogEvent> events;
    private final String functionName;
    private final String logStream;
    private final String requestId;

    /**
     * Constructor of a function execution event. Consists of a function name and a request Id.
     */
    public FunctionExecutionEvent(final String functionName, final String logStream, final String requestId) {
        this.events = new ArrayList<>();
        this.functionName = functionName;
        this.logStream = logStream;
        this.requestId = requestId;
    }

    /**
     * Adds a single event (log message) to the execution event.
     */
    public void addLogEvent(final OutputLogEvent event) {
        this.events.add(event);
    }

    /**
     * Getter for the request Id (function execution request from AmazonCloudWatch). This request Id is renamed in our
     * application to platform id!
     *
     * @return the request of the function execution.
     */
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * Getter for the {@link OutputLogEvent}s.
     */
    public List<OutputLogEvent> getEvents() {
        return this.events;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    /**
     * Generates a unique file name for the naming of the java test classes. "-" are a problem for the class names.
     * Therefore the "-" are replaced with "_".
     *
     * @return a unique filename based on the function name and request Id
     */
    public String getUniqueFileName() {
        final String uniqueFileName = this.functionName + "_" + this.requestId;
        return uniqueFileName.replace("-", "_");
    }

    public String getLogStream() {
        return this.logStream;
    }

    @Override
    public String toString() {
        return "FunctionExecutionEvent [events=" + this.events + "]";
    }

    /**
     * Test, if the log data, encapsulated in the message part of the {@link OutputLogEvent}s.
     *
     * @return true, if a single message contains the search string <br/> false, otherwise
     */
    public boolean containsSearchString(final String searchString) {

        if (searchString == null || searchString.trim().isEmpty()) {
            return true;
        }

        for (final OutputLogEvent event : this.events) {
            if (event.getMessage().contains(searchString.trim())) {
                return true;
            }
        }
        return false;
    }
}
