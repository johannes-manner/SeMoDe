package de.uniba.dsg.serverless.semode.model;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.logs.model.OutputLogEvent;

/**
 * This class is a model for all log and related data, like start time, end time and consumption of 
 * time and memory. Because of the string based nature of log data in AmazonCloudWatch, this model class groups
 * all data associated to a single function execution to get a cohesive data structure for log events.
 * 
 * @author Johannes Manner
 * 
 * @version 1.0
 *
 */
public final class FunctionExecutionEvent {
	
	private final List<OutputLogEvent> events;
	private final String functionName;
	private final String logStream;
	private final String requestId;

	/**
	 * Constructor of a function execution event. 
	 * Consists of a function name and a request Id.
	 * 
	 * @param functionName
	 * @param requestId
	 */
	public FunctionExecutionEvent(String functionName, String logStream, String requestId) {
		this.events = new ArrayList<>();
		this.functionName = functionName;
		this.logStream = logStream;
		this.requestId = requestId;
	}

	/**
	 * Adds a single event (log message) to the execution event.
	 * 
	 * @param event
	 */
	public void addLogEvent(OutputLogEvent event) {
		this.events.add(event);
	}

	/**
	 * Getter for the request Id (function execution request from AmazonCloudWatch)
	 * 
	 * @return 
	 * 		the request of the function execution.
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * Getter for the {@link OutputLogEvent}s.
	 * 
	 * @return
	 */
	public List<OutputLogEvent> getEvents() {
		return events;
	}
	
	public String getFunctionName() {
		return this.functionName;
	}
	
	/**
	 * Generates a unique file name for the naming of the java test classes.
	 * 
	 * @return
	 * 		a unique filename based on the function name and request Id
	 */
	public String getUniqueFileName() {
		return this.functionName + "_" + this.requestId;
	}
	
	public String getLogStream() {
		return this.logStream;
	}

	@Override
	public String toString() {
		return "FunctionExecutionEvent [events=" + events + "]";
	}

	/**
	 * Test, if the log data, encapsulated in the message part of the {@link OutputLogEvent}s.
	 * 
	 * @param searchString
	 * @return
	 * 		true, if a single message contains the search string <br/>
	 * 		false, otherwise
	 */
	public boolean containsSearchString(String searchString) {
		
		if(searchString == null || searchString.trim().isEmpty()) {
			return true;
		}
		
		for (OutputLogEvent event : events) {
			if (event.getMessage().contains(searchString.trim())) {
				return true;
			}
		}
		return false;
	}

}
