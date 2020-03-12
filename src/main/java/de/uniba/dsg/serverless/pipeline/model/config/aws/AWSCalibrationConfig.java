package de.uniba.dsg.serverless.pipeline.model.config.aws;

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

public class AWSCalibrationConfig {

    public static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources");
    // Constants
    private static final Gson PARSER = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    // AWS specific information
    @Expose
    public boolean deployLinpack;
    @Expose
    public String bucketName;
    @Expose
    public int numberOfAWSExecutions;
    @Expose
    public boolean enabled;
    @Expose
    public AWSDeploymentInternals deploymentInternals;
    @Expose
    public AWSFunctionConfig functionConfig;

    private AWSCalibrationConfig() {
        // hide default Constructor
    }

    // copy constructor
    public AWSCalibrationConfig(final AWSCalibrationConfig config) {
        this.deployLinpack = config.deployLinpack;
        this.bucketName = config.bucketName;
        this.numberOfAWSExecutions = config.numberOfAWSExecutions;
        this.enabled = config.enabled;
        this.functionConfig = new AWSFunctionConfig(config.functionConfig);
        this.deploymentInternals = new AWSDeploymentInternals(config.deploymentInternals);
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

    public void update(final String region, final String runtime, final String awsArnLambdaRole, final String functionHandler,
                       final String timeout, final String deployLinpack, final String targetUrl, final String apiKey,
                       final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled, final String pathToSource) throws IOException {
        if (!"".equals(deployLinpack)) this.deployLinpack = Boolean.parseBoolean(deployLinpack);
        if (!"".equals(bucketName)) this.bucketName = bucketName;
        if (!"".equals(numberOfAWSExecutions)) this.numberOfAWSExecutions = Integer.parseInt(numberOfAWSExecutions);
        if (!"".equals(enabled)) this.enabled = Boolean.parseBoolean(enabled);
        this.functionConfig.update(region, runtime, awsArnLambdaRole, functionHandler, timeout, targetUrl, apiKey, memorySizes, pathToSource);
    }
}


