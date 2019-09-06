package de.uniba.dsg.serverless.model;

import java.time.LocalDateTime;

public class LocalRESTEvent implements WritableEvent {

	private String platformId;
	private String containerId;
	private String vmIdentification;
	private String cpuModel;
	private String cpuModelName;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private boolean erroneous = false;

	public LocalRESTEvent() {
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

	public String getVmIdentification() {
		return vmIdentification;
	}

	public void setVmIdentification(String vmIdentification) {
		this.vmIdentification = vmIdentification;
	}

	public String getCpuModel() {
		return cpuModel;
	}

	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}

	public String getCpuModelName() {
		return cpuModelName;
	}

	public void setCpuModelName(String cpuModelName) {
		this.cpuModelName = cpuModelName;
	}

	@Override
	public String getCSVMetadata() {
		return "PlatformId" + CSV_SEPARATOR + "ContainerId" + CSV_SEPARATOR + "VM_Identification" + CSV_SEPARATOR
				+ "CPU_Model" + CSV_SEPARATOR + "CPU_Model_Name" + CSV_SEPARATOR + "StartRESTTime" + CSV_SEPARATOR
				+ "EndRESTTime" + CSV_SEPARATOR + "Erroneous" + CSV_SEPARATOR;
	}

	@Override
	public String toCSVString() {
		return this.platformId + CSV_SEPARATOR + this.containerId + CSV_SEPARATOR + this.vmIdentification
				+ CSV_SEPARATOR + this.cpuModel + CSV_SEPARATOR + this.cpuModelName + CSV_SEPARATOR
				+ this.startTime.format(CSV_FORMATTER) + CSV_SEPARATOR + this.endTime.format(CSV_FORMATTER)
				+ CSV_SEPARATOR + this.erroneous + CSV_SEPARATOR;
	}

}
