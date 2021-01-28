package de.uniba.dsg.serverless.pipeline.model.config.aws;

import lombok.Data;

@Data
public class AWSCalibrationConfig {

    // AWS specific information
    private String bucketName;
    private int numberOfAWSExecutions;

    private AWSBenchmarkConfig benchmarkConfig;

    public AWSCalibrationConfig() {
        // hide default Constructor
    }

    /**
     * Resets the system generated values and the identifier and names from the aws cloud platform. Leaves the
     * <i>settings.json</i> in a consistent state. <br/> If you alter this method, also check {@link
     * AWSBenchmarkConfig#resetConfig()}.
     */
    public void resetConfig() {
        this.benchmarkConfig.resetConfig();
    }
}


