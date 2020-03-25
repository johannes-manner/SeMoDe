package de.uniba.dsg.serverless.pipeline.model.config;

import com.google.common.primitives.Ints;
import com.google.gson.annotations.Expose;
import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class for benchmark execution config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class BenchmarkConfig {

    // corePoolSize for executor service
    @Expose
    public Integer numberThreads;
    @Expose
    public String benchmarkMode;
    @Expose
    public String benchmarkParameters;
    @Expose
    public String postArgument;
    @Expose
    public String startTime;
    @Expose
    public String endTime;

    // aws parameters
    @Expose
    public AWSBenchmarkConfig awsBenchmarkConfig;

    public BenchmarkConfig() {
    }

    public AWSBenchmarkConfig getAwsBenchmarkConfig() {
        if (this.awsBenchmarkConfig == null) {
            this.awsBenchmarkConfig = new AWSBenchmarkConfig();
        }
        return this.awsBenchmarkConfig;
    }

    public void update(final String numberOfThreads, final String benchmarkingMode, final String benchmarkingParameters, final String postArgument) {
        if (!"".equals(numberOfThreads) && Ints.tryParse(numberOfThreads) != null) {
            this.numberThreads = Ints.tryParse(numberOfThreads);
        }

        if (!"".equals(benchmarkingMode) && List.of(BenchmarkMode.values()).stream().map(BenchmarkMode::getText).collect(Collectors.toList()).contains(benchmarkingMode)) {
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
     * Logs the start time, when the benchmark is started.
     * Value is needed for a later retrieval of the information from the corresponding platform.
     */
    public void logBenchmarkStartTime() {
        this.startTime = LocalDateTime.now().toString();
    }

    /**
     * Logs the end time, when the benchmark is finished and the last execution terminated.
     * Value is needed for a later retrieval of the information from the corresponding platform.
     */
    public void logBenchmarkEndTime() {
        this.endTime = LocalDateTime.now().toString();
    }

    @Override
    public String toString() {
        return "BenchmarkConfig{" +
                "numberThreads=" + this.numberThreads +
                ", benchmarkMode='" + this.benchmarkMode + '\'' +
                ", benchmarkParameters='" + this.benchmarkParameters + '\'' +
                ", postArgument='" + this.postArgument + '\'' +
                ", awsBenchmarkConfig=" + this.awsBenchmarkConfig +
                '}';
    }
}
