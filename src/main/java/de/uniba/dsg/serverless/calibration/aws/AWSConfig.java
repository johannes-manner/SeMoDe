package de.uniba.dsg.serverless.calibration.aws;

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
import java.util.List;

public class AWSConfig {

    // Constants
    private static final Gson PARSER = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    public static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources");

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

    private AWSConfig() {
        // hide default Constructor
    }

    /**
     * Loads an experiment configuration from a file
     *
     * @param fileName experiment file
     * @return Experiment
     * @throws SeMoDeException if the file is corrupt or does not exist
     */
    public static AWSConfig fromFile(String fileName) throws SeMoDeException {
        AWSConfig experiment;
        try {
            Reader reader = new BufferedReader(new FileReader(RESOURCES_FOLDER.resolve(fileName).toString()));
            experiment = PARSER.fromJson(reader, AWSConfig.class);
        } catch (IOException e) {
            throw new SeMoDeException("File does not exist or is corrupt.", e);
        }
        return experiment;
    }

}
