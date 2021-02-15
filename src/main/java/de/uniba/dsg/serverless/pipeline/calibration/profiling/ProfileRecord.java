package de.uniba.dsg.serverless.pipeline.calibration.profiling;

import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDateTime;

@Slf4j
@Data
@Entity
@NoArgsConstructor
public class ProfileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // here for mapping the execution to the logs at the local machine!
    // might be interesting in the case some errors etc happen
    private String randomExecutionNumber;
    private String functionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationInMS;
    private long memorySize;
    private long memoryUsed;

    public ProfileRecord(String randomExecutionNumber, String functionName, LocalDateTime startTime, LocalDateTime endTime, long durationInMS, long memorySize, long memoryUsed) {
        this.randomExecutionNumber = randomExecutionNumber;
        this.functionName = functionName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationInMS = durationInMS;
        this.memorySize = memorySize;
        this.memoryUsed = memoryUsed;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    private CalibrationConfig calibrationConfig;
}
