package de.uniba.dsg.serverless.model;

import java.time.LocalDateTime;

public class LocalRESTEvent {

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

}
