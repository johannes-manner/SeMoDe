package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.google.gson.annotations.Expose;

public class AWSBenchmarkConfig {
    @Expose
    public AWSDeploymentInternals deploymentInternals;
    @Expose
    public AWSFunctionConfig functionConfig;

    public AWSBenchmarkConfig() {
    }

    public AWSFunctionConfig getFunctionConfig() {
        if (this.functionConfig == null) {
            this.functionConfig = new AWSFunctionConfig();
        }
        return this.functionConfig;
    }

    public AWSDeploymentInternals getDeploymentInternals() {
        if (this.deploymentInternals == null) {
            this.deploymentInternals = new AWSDeploymentInternals();
        }
        return this.deploymentInternals;
    }

    /**
     * Resets the system generated values and the identifier and names
     * from the aws cloud platform. Leaves the <i>settings.json</i> in a
     * consistent state.
     * <br/>
     * If you alter this method, also check {@link AWSCalibrationConfig#resetConfig()}.
     */
    public void resetConfig() {
        this.deploymentInternals.reset();
        this.functionConfig.reset();
    }

    /**
     * Only initialized, when the generated system values are present, e.g. the
     * deployment internals. Otherwise not deployed or initialized.
     *
     * @return
     */
    public boolean isInitialized() {
        return this.deploymentInternals.isInitialized();
    }

    @Override
    public String toString() {
        return "AWSBenchmarkConfig{" +
                "deploymentInternals=" + this.deploymentInternals +
                ", functionConfig=" + this.functionConfig +
                '}';
    }
}
