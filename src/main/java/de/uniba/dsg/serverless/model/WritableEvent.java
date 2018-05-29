package de.uniba.dsg.serverless.model;

import java.time.format.DateTimeFormatter;

public interface WritableEvent {
	
	public static final String CSV_SEPARATOR = System.getProperty("CSV_SEPARATOR");
	// TODO : Change to System property - think about the dependencies
	public static final DateTimeFormatter CSV_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");


	public String getCSVMetadata();
	
	public String toCSVString();
}
