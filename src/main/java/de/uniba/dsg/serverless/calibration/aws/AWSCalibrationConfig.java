package de.uniba.dsg.serverless.calibration.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private AWSCalibrationConfig() {
        // hide default Constructor
    }

    public AWSCalibrationConfig(final AWSCalibrationConfig config) {
        this.targetUrl = config.targetUrl;
        this.apiKey = config.apiKey;
        this.bucketName = config.bucketName;
        this.memorySizes = List.copyOf(config.memorySizes);
        this.numberOfAWSExecutions = config.numberOfAWSExecutions;
        this.enabled = config.enabled;
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

    public void update(final String targetUrl, final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled) throws IOException {
        if (!"".equals(targetUrl)) this.targetUrl = targetUrl;
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
                "targetUrl='" + this.targetUrl + '\'' +
                ", apiKey='" + this.apiKey + '\'' +
                ", bucketName='" + this.bucketName + '\'' +
                ", memorySizes=" + this.memorySizes +
                ", numberOfAWSExecutions=" + this.numberOfAWSExecutions +
                ", enabled=" + this.enabled +
                '}';
    }
}
