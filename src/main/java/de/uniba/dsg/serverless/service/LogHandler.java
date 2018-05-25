package de.uniba.dsg.serverless.service;

import java.util.Map;

import de.uniba.dsg.serverless.model.LocalRESTEvent;

public interface LogHandler {

	public void addLocalRESTEvents(Map<String, LocalRESTEvent> localRESTEvents);
	
	public void writePerformanceDataToFile(String fileName);
}
