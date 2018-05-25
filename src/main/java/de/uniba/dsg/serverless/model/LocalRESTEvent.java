package de.uniba.dsg.serverless.model;

import java.time.LocalDateTime;

public class LocalRESTEvent implements WritableEvent{

	private final String platformId;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;

	public LocalRESTEvent(String platformId, LocalDateTime startTime, LocalDateTime endTime) {
		super();
		this.platformId = platformId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getPlatformId() {
		return platformId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	@Override
	public String toString() {
		return "LocalRESTEvent [platformId=" + platformId + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}

	@Override
	public String getCSVMetadata() {
		return "PlatformId" + CSV_SEPARATOR + "StartRESTTime" + CSV_SEPARATOR + "EndRESTTime" + CSV_SEPARATOR;
	}

	@Override
	public String toCSVString() {
		return this.platformId + CSV_SEPARATOR + this.startTime.format(CSV_FORMATTER) + CSV_SEPARATOR + this.endTime.format(CSV_FORMATTER) + CSV_SEPARATOR;
	}

}
