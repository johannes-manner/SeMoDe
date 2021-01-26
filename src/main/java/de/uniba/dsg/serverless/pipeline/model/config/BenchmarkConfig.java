package de.uniba.dsg.serverless.pipeline.model.config;

import com.google.gson.annotations.Expose;
import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Model class for benchmark execution config and json serialization. DO NOT change this class. Otherwise json
 * serialization and deserialization does not work properly.
 */
// TODO add bean validation to the class (not completed yet)
@Data
public class BenchmarkConfig {

    @Expose
    @NotNull(message = "Select one of the benchmark modes")
    public String benchmarkMode;
    @Expose
    // TODO refactor this
    public String benchmarkParameters;
    @Expose
    // TODO check the usage here...
    public String postArgument;

    // TODO i don't know why Local Date Time is a problem - might be json parsing problem
    @Expose
    public String startTime;
    @Expose
    public String endTime;

    // aws parameters
    @Expose
    public AWSBenchmarkConfig awsBenchmarkConfig;

    public BenchmarkConfig() {
        this.awsBenchmarkConfig = new AWSBenchmarkConfig();
    }

    public void update(final String numberOfThreads, final String benchmarkingMode, final String benchmarkingParameters, final String postArgument) {

        if (!"".equals(benchmarkingMode) && BenchmarkMode.availableModes.stream().map(BenchmarkMode::getText).collect(Collectors.toList()).contains(benchmarkingMode)) {
            this.benchmarkMode = benchmarkingMode;
        }
        // TODO validation difficult here . . .
        if (!"".equals(benchmarkingParameters)) {
            this.benchmarkParameters = benchmarkingParameters;
        }

        if (!"".equals(postArgument)) {
            this.postArgument = postArgument;
        }
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
