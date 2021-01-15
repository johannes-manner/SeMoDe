package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class AWSBenchmarkConfig {
    @Expose
    public AWSDeploymentInternals deploymentInternals;
    @Expose
    public AWSFunctionConfig functionConfig;

    public AWSBenchmarkConfig() {
        this.deploymentInternals = new AWSDeploymentInternals();
        this.functionConfig = new AWSFunctionConfig();
    }

    /**
     * Resets the system generated values and the identifier and names from the aws cloud platform. Leaves the
     * <i>settings.json</i> in a consistent state. <br/> If you alter this method, also check {@link
     * AWSCalibrationConfig#resetConfig()}.
     */
    public void resetConfig() {
        this.deploymentInternals.reset();
        this.functionConfig.reset();
    }
}
