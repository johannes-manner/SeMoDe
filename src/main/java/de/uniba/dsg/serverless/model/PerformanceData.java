package de.uniba.dsg.serverless.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PerformanceData {

	private final String functionName;
	private final String logStream;
	private final String requestId;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;
	private final double startupDuration;
	private final double preciseDuration;
	private final int billedDuration;
	private final int memorySize;
	private final int memoryUsed;

	private static final String CSV_SEPARATOR = System.getProperty("CSV_SEPARATOR");
	private static final DateTimeFormatter CSV_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");

	public PerformanceData() {
		this.functionName = "";
		this.logStream = "";
		this.requestId = "";
		this.startTime = LocalDateTime.MIN;
		this.endTime = LocalDateTime.MIN;
		this.startupDuration = -1;
		this.preciseDuration = -1;
		this.billedDuration = -1;
		this.memorySize = -1;
		this.memoryUsed = -1;
	}

	public PerformanceData(String functionName, String logStream, String requestId, LocalDateTime startTime,
			LocalDateTime endTime, double startupDuration, double preciseDuration, int billedDuration, int memorySize,
			int memoryUsed) {
		this.functionName = functionName;
		this.logStream = logStream;
		this.requestId = requestId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startupDuration = startupDuration;
		this.preciseDuration = preciseDuration;
		this.billedDuration = billedDuration;
		this.memorySize = memorySize;
		this.memoryUsed = memoryUsed;
	}

	@Override
	public String toString() {
		return "PerformanceData [functionName=" + functionName + ", logStream=" + logStream + ", requestId=" + requestId
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", startupDuration=" + startupDuration
				+ ", preciseDuration=" + preciseDuration + ", billedDuration=" + billedDuration + ", memorySize="
				+ memorySize + ", memoryUsed=" + memoryUsed + "]";
	}

	public static String getCSVMetadata() {
		return "FunctionName" + CSV_SEPARATOR + "LogStream" + CSV_SEPARATOR + "RequestID" + CSV_SEPARATOR + "StartTime"
				+ CSV_SEPARATOR + "EndTime" + CSV_SEPARATOR + "StartupDuration" + CSV_SEPARATOR + "PreciseDuration"
				+ CSV_SEPARATOR + "BilledDuration" + CSV_SEPARATOR + "MemorySize" + CSV_SEPARATOR + "MemoryUsed";
	}

	public String toCSVString() {
		return this.functionName + CSV_SEPARATOR + this.logStream + CSV_SEPARATOR + this.requestId + CSV_SEPARATOR
				+ this.startTime.format(CSV_FORMATTER) + CSV_SEPARATOR + this.endTime.format(CSV_FORMATTER)
				+ CSV_SEPARATOR + this.startupDuration + CSV_SEPARATOR + this.preciseDuration + CSV_SEPARATOR
				+ this.billedDuration + CSV_SEPARATOR + this.memorySize + CSV_SEPARATOR + this.memoryUsed;
	}

}
