package de.uniba.dsg.serverless.calibration.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AWSCalibrationConfig {

    public static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources");
    // Constants
    private static final Gson PARSER = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    // AWS specific information
    @Expose
    public String region;
    @Expose
    public String runtime;
    @Expose
    public String awsArnLambdaRole;
    @Expose
    public String functionHandler;
    @Expose
    public int timeout;
    @Expose
    public boolean deployLinpack;
    @Expose
    public String targetUrl;
    @Expose
    public String apiKey;
    @Expose
    public String bucketName;
    @Expose
    public List<Integer> memorySizes;
    @Expose
    public int numberOfAWSExecutions;
    @Expose
    public boolean enabled;
    @Expose
    public DeploymentInternals deploymentInternals;

    private AWSCalibrationConfig() {
        // hide default Constructor
    }

    public AWSCalibrationConfig(final AWSCalibrationConfig config) {
        this.region = config.region;
        this.runtime = config.runtime;
        this.awsArnLambdaRole = config.awsArnLambdaRole;
        this.functionHandler = config.functionHandler;
        this.timeout = config.timeout;
        this.deployLinpack = config.deployLinpack;
        this.targetUrl = config.targetUrl;
        this.apiKey = config.apiKey;
        this.bucketName = config.bucketName;
        this.memorySizes = List.copyOf(config.memorySizes);
        this.numberOfAWSExecutions = config.numberOfAWSExecutions;
        this.enabled = config.enabled;
        this.deploymentInternals = new DeploymentInternals(config.deploymentInternals);
    }

    /**
     * Loads an experiment configuration from a file
     *
     * @param fileName experiment file
     * @return Experiment
     * @throws SeMoDeException if the file is corrupt or does not exist
     */
    public static AWSCalibrationConfig fromFile(final String fileName) throws SeMoDeException {
        final AWSCalibrationConfig experiment;
        try {
            final Reader reader = new BufferedReader(new FileReader(RESOURCES_FOLDER.resolve(fileName).toString()));
            experiment = PARSER.fromJson(reader, AWSCalibrationConfig.class);
        } catch (final IOException e) {
            throw new SeMoDeException("File does not exist or is corrupt.", e);
        }
        return experiment;
    }

    public void update(final String region, final String runtime, final String awsArnLambdaRole, final String functionHandler, final String timeout, final String deployLinpack, final String targetUrl, final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled) throws IOException {
        if (!"".equals(region)) this.region = region;
        if (!"".equals(runtime)) this.runtime = runtime;
        if (!"".equals(awsArnLambdaRole)) this.awsArnLambdaRole = awsArnLambdaRole;
        if (!"".equals(functionHandler)) this.functionHandler = functionHandler;
        if (!"".equals(timeout) && Ints.tryParse(timeout) != null) this.timeout = Ints.tryParse(timeout);
        if (!"".equals(deployLinpack)) this.deployLinpack = Boolean.parseBoolean(deployLinpack);
        if (!"".equals(targetUrl)) this.targetUrl = this.targetUrl;
        if (!"".equals(apiKey)) this.apiKey = apiKey;
        if (!"".equals(bucketName)) this.bucketName = bucketName;
        if (!"".equals(memorySizes))
            this.memorySizes = (List<Integer>) new ObjectMapper().readValue(memorySizes, ArrayList.class);
        if (!"".equals(numberOfAWSExecutions)) this.numberOfAWSExecutions = Integer.parseInt(numberOfAWSExecutions);
        if (!"".equals(enabled)) this.enabled = Boolean.parseBoolean(enabled);

    }

    @Override
    public String toString() {
        return "AWSCalibrationConfig{" +
                "region='" + this.region + '\'' +
                ", runtime='" + this.runtime + '\'' +
                ", awsArnLambdaRole='" + this.awsArnLambdaRole + '\'' +
                ", functionHandler='" + this.functionHandler + '\'' +
                ", timeout=" + this.timeout +
                ", deployLinpack=" + this.deployLinpack +
                ", targetUrl='" + this.targetUrl + '\'' +
                ", apiKey='" + this.apiKey + '\'' +
                ", bucketName='" + this.bucketName + '\'' +
                ", memorySizes=" + this.memorySizes +
                ", numberOfAWSExecutions=" + this.numberOfAWSExecutions +
                ", enabled=" + this.enabled +
                ", deploymentInternals=" + this.deploymentInternals +
                '}';
    }
}

/**
 * Simple POJO class to store the internals during the deployment of the resources on the aws platform.
 * The data is needed for a later removal of the resources via function calls.
 */
class DeploymentInternals {

    @Expose
    public String restApiId;
    @Expose
    public String apiKeyId;
    @Expose
    public String usagePlanId;

    private DeploymentInternals() {

    }

    public DeploymentInternals(final DeploymentInternals other) {
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
