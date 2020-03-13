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

    @Override
    public String toString() {
        return "AWSBenchmarkConfig{" +
                "deploymentInternals=" + this.deploymentInternals +
                ", functionConfig=" + this.functionConfig +
                '}';
    }
}
