package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.google.gson.annotations.Expose;
import lombok.Data;

// TODO Expose (search for)
@Data
public class AWSCalibrationConfig {

    // AWS specific information
    @Expose
    public String bucketName;
    @Expose
    public int numberOfAWSExecutions;

    public AWSBenchmarkConfig benchmarkConfig;

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


