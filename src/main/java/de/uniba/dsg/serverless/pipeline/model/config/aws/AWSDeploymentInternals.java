package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.google.gson.annotations.Expose;

/**
 * Simple POJO class to store the internals during the deployment of the resources on the aws platform.
 * The data is needed for a later removal of the resources via function calls.
 */
public class AWSDeploymentInternals {

    @Expose
    public String restApiId;
    @Expose
    public String apiKeyId;
    @Expose
    public String usagePlanId;

    private AWSDeploymentInternals() {

    }

    public AWSDeploymentInternals(final AWSDeploymentInternals other) {
        this.restApiId = other.restApiId;
        this.apiKeyId = other.apiKeyId;
        this.usagePlanId = other.usagePlanId;
    }

    @Override
    public String toString() {
        return "DeploymentInternals{" +
                "restApiId='" + this.restApiId + '\'' +
                ", apiKeyId='" + this.apiKeyId + '\'' +
                ", usagePlanId='" + this.usagePlanId + '\'' +
                '}';
    }
}