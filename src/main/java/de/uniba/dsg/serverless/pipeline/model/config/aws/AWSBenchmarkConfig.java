package de.uniba.dsg.serverless.pipeline.model.config.aws;

import lombok.Data;

import javax.persistence.Embeddable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@Embeddable
public class AWSBenchmarkConfig {

    // user input
    private String awsDescription;
    private String region;
    private String runtime;
    private String awsArnLambdaRole;
    private String functionHandler;
    private int timeout;
    private String memorySizes;
    private String deploymentPackageSizes;
    private String pathToSource;

    // internals set by aws
    private String targetUrl;
    private String apiKey;
    private String restApiId;
    private String apiKeyId;
    private String usagePlanId;

    public AWSBenchmarkConfig() {
    }

    /**
     * Resets the system generated values and the identifier and names from the aws cloud platform. Leaves the
     * <i>settings.json</i> in a consistent state. <br/> If you alter this method, also check {@link
     * AWSCalibrationConfig#resetConfig()}.
     */
    public void resetConfig() {
        this.targetUrl = "";
        this.apiKey = "";
        this.restApiId = "";
        this.apiKeyId = "";
        this.usagePlanId = "";
    }

    public List<Integer> getMemorySizeList() {
        if (this.memorySizes == null) {
            return List.of();
        }
        return Arrays.stream(this.memorySizes.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<String> getDeploymentSizes() {

        if (this.memorySizes == null) {
            return List.of("0");
        }

        return Arrays.stream(this.deploymentPackageSizes.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());
    }
}
