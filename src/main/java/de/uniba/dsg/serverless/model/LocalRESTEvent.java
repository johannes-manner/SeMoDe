package de.uniba.dsg.serverless.model;

import java.time.LocalDateTime;

public class LocalRESTEvent implements WritableEvent {

	private String platformId;
	private String containerId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private boolean erroneous = false;

	public LocalRESTEvent() {
	}

	public LocalRESTEvent(String platformId, String containerId, LocalDateTime startTime, LocalDateTime endTime) {
		super();
		this.platformId = platformId;
		this.containerId = containerId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalDateTime endTime) {
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

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public boolean isErroneous() {
		return erroneous;
	}

	public void setErroneous(boolean erroneous) {
		this.erroneous = erroneous;
	}

	@Override
	public String getCSVMetadata() {
		return "PlatformId" + CSV_SEPARATOR + "ContainerId" + CSV_SEPARATOR + "StartRESTTime" + CSV_SEPARATOR + "EndRESTTime" + CSV_SEPARATOR + "Erroneous" + CSV_SEPARATOR;
	}

	@Override
	public String toCSVString() {
		return this.platformId + CSV_SEPARATOR + this.containerId + CSV_SEPARATOR + this.startTime.format(CSV_FORMATTER) + CSV_SEPARATOR
				+ this.endTime.format(CSV_FORMATTER) + CSV_SEPARATOR + this.erroneous + CSV_SEPARATOR;
	}

}
