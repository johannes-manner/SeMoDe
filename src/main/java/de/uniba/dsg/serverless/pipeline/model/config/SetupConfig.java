package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

/**
 * <p>
 * This is the central configuration class were all config values for the pipeline is stored. <br/> Currently
 * benchmarking and calibration is supported. Start the web application and configure the setup with respect to the
 * information shown there.
 * </p>
 * <p>
 * Do NOT change this class, despite you want to introduce new functionality to the pipeline. Make a pull request
 * therefore :)
 * </p>
 */
@Data
public class SetupConfig {

    private String setupName;
    private BenchmarkConfig benchmarkConfig;
    private CalibrationConfig calibrationConfig;

    public SetupConfig() {
        this.benchmarkConfig = new BenchmarkConfig();
        this.calibrationConfig = new CalibrationConfig();
    }

    public SetupConfig(String name) {
        this.setupName = name;
        this.benchmarkConfig = new BenchmarkConfig();
        this.calibrationConfig = new CalibrationConfig();
    }
}