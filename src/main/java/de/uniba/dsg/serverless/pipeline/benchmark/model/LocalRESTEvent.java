package de.uniba.dsg.serverless.pipeline.benchmark.model;

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

    @Override
    public String getPlatformId() {
        return this.platformId;
    }

    public void setPlatformId(final String platformId) {
        this.platformId = platformId;
    }

    @Override
    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getContainerId() {
        return this.containerId;
    }

    public void setContainerId(final String containerId) {
        this.containerId = containerId;
    }

    public boolean isErroneous() {
        return this.erroneous;
    }

    public void setErroneous(final boolean erroneous) {
        this.erroneous = erroneous;
    }

    public String getVmIdentification() {
        return this.vmIdentification;
    }

    public void setVmIdentification(final String vmIdentification) {
        this.vmIdentification = vmIdentification;
    }

    public String getCpuModel() {
        return this.cpuModel;
    }

    public void setCpuModel(final String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public String getCpuModelName() {
        return this.cpuModelName;
    }

    public void setCpuModelName(final String cpuModelName) {
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
