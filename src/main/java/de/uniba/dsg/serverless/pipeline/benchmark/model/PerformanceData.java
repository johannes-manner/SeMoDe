package de.uniba.dsg.serverless.pipeline.benchmark.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class PerformanceData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String functionName;
    private String logStream;
    private String platformId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double startupDuration;
    private double preciseDuration;
    private int billedDuration;
    private int memorySize;
    private int memoryUsed;

    public PerformanceData() {
        this.functionName = "";
        this.logStream = "";
        this.platformId = "";
        this.startTime = LocalDateTime.MIN;
        this.endTime = LocalDateTime.MIN;
        this.startupDuration = -1;
        this.preciseDuration = -1;
        this.billedDuration = -1;
        this.memorySize = -1;
        this.memoryUsed = -1;
    }

    public PerformanceData(final String functionName, final String logStream, final String platformId, final LocalDateTime startTime,
                           final LocalDateTime endTime, final double startupDuration, final double preciseDuration, final int billedDuration, final int memorySize,
                           final int memoryUsed) {
        this.functionName = functionName;
        this.logStream = logStream;
        this.platformId = platformId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startupDuration = startupDuration;
        this.preciseDuration = preciseDuration;
        this.billedDuration = billedDuration;
        this.memorySize = memorySize;
        this.memoryUsed = memoryUsed;
    }
}
