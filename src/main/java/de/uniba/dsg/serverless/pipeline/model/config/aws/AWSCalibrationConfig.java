package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.io.IOException;

// TODO Expose (search for)
@Data
public class AWSCalibrationConfig {

    // AWS specific information
    @Expose
    public String bucketName;
    @Expose
    public int numberOfAWSExecutions;
    @Expose
    public AWSDeploymentInternals deploymentInternals;
    @Expose
    public AWSFunctionConfig functionConfig;

    public AWSCalibrationConfig() {
        // hide default Constructor
    }


    public void update(final String region, final String runtime, final String awsArnLambdaRole, final String functionHandler,
                       final String timeout, final String deployLinpack, final String targetUrl, final String apiKey,
                       final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled, final String pathToSource) throws IOException {
        if (!"".equals(bucketName)) this.bucketName = bucketName;
        if (!"".equals(numberOfAWSExecutions)) this.numberOfAWSExecutions = Integer.parseInt(numberOfAWSExecutions);
        this.functionConfig.update(region, runtime, awsArnLambdaRole, functionHandler, timeout, targetUrl, apiKey, memorySizes, pathToSource);
    }

    /**
     * Resets the system generated values and the identifier and names from the aws cloud platform. Leaves the
     * <i>settings.json</i> in a consistent state. <br/> If you alter this method, also check {@link
     * AWSBenchmarkConfig#resetConfig()}.
     */
    public void resetConfig() {
        this.deploymentInternals.reset();
        this.functionConfig.reset();
    }
}


