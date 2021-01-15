package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.google.gson.annotations.Expose;
import lombok.Data;

/**
 * Simple POJO class to store the internals during the deployment of the resources on the aws platform. The data is
 * needed for a later removal of the resources via function calls.
 */
// TODO add bean validation
// TODO include more docu links...
@Data
public class AWSDeploymentInternals {

    @Expose
    public String restApiId;
    @Expose
    public String apiKeyId;
    @Expose
    public String usagePlanId;

    public AWSDeploymentInternals() {

    }

    public AWSDeploymentInternals(final AWSDeploymentInternals other) {
        this.restApiId = other.restApiId;
        this.apiKeyId = other.apiKeyId;
        this.usagePlanId = other.usagePlanId;
    }

    /**
     * Resets all the internal values, helpful for documentation purposes to not confuse the user.
     */
    public void reset() {
        this.restApiId = "";
        this.apiKeyId = "";
        this.usagePlanId = "";
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