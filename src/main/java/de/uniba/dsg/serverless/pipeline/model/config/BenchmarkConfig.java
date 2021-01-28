package de.uniba.dsg.serverless.pipeline.model.config;

import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Model class for benchmark execution config and json serialization. DO NOT change this class. Otherwise json
 * serialization and deserialization does not work properly.
 */
// TODO add bean validation to the class (not completed yet)
@Data
public class BenchmarkConfig {

    @NotNull(message = "Select one of the benchmark modes")
    private String benchmarkMode;
    private String benchmarkParameters;
    private String postArgument;

    // TODO i don't know why Local Date Time is a problem - might be json parsing problem
    // TODO when changing it to an entity, go for it in the db :)
    private String startTime;
    private String endTime;

    // aws parameters
    private AWSBenchmarkConfig awsBenchmarkConfig;

    public BenchmarkConfig() {
        this.awsBenchmarkConfig = new AWSBenchmarkConfig();
    }


    /**
     * Logs the start time, when the benchmark is started. Value is needed for a later retrieval of the information from
     * the corresponding platform.
     */
    public void logBenchmarkStartTime() {
        this.startTime = LocalDateTime.now().toString();
    }

    /**
     * Logs the end time, when the benchmark is finished and the last execution terminated. Value is needed for a later
     * retrieval of the information from the corresponding platform.
     */
    public void logBenchmarkEndTime() {
        this.endTime = LocalDateTime.now().toString();
    }
}
