package de.uniba.dsg.serverless.pipeline.rest.dto;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class BenchmarkConfigDTO {

    private Long benchmarkId;
    private String setupName;
    private String experimentDescription;
    private String benchmarkingMode;
    private String benchmarkingParameters;
    private String postBody;
    private String experimentTime;
    private String functionDescription;
    private String region;
    private String runtime;
    private String handlerName;
    private int timeout;
    private String memorySizes;

    public BenchmarkConfigDTO(BenchmarkConfig benchmarkConfig) {
        this.benchmarkId = benchmarkConfig.getId();
        this.setupName = benchmarkConfig.getSetupName();
        this.experimentDescription = benchmarkConfig.getDescription();
        this.benchmarkingMode = benchmarkConfig.getBenchmarkMode();
        this.benchmarkingParameters = benchmarkConfig.getBenchmarkParameters();
        this.postBody = benchmarkConfig.getPostArgument();
        this.experimentTime =
                benchmarkConfig.getStartTime() != null ? benchmarkConfig.getStartTime().toString() : ""
                        + " - " +
                        benchmarkConfig.getEndTime() != null ? benchmarkConfig.getEndTime().toString() : "";
        this.functionDescription = benchmarkConfig.getAwsBenchmarkConfig().getAwsDescription();
        this.region = benchmarkConfig.getAwsBenchmarkConfig().getRegion();
        this.runtime = benchmarkConfig.getAwsBenchmarkConfig().getRuntime();
        this.handlerName = benchmarkConfig.getAwsBenchmarkConfig().getFunctionHandler();
        this.timeout = benchmarkConfig.getAwsBenchmarkConfig().getTimeout();
        this.memorySizes = benchmarkConfig.getAwsBenchmarkConfig().getMemorySizes();
    }
}
