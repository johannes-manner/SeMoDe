package de.uniba.dsg.serverless.pipeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import de.uniba.dsg.serverless.calibration.local.LocalCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.AWSBenchmark;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.model.config.*;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to change the attributes of the user config class.
 * If the changes are made directly in the model classes, there occur json parsing errors.
 */
public class UserConfigHandler {

    private UserConfig userConfig;

    public UserConfigHandler() {
        this.userConfig = new UserConfig();
    }

    public void updateAWSConfig(final String region, final String runtime, final String awsArnRole, final String functionHandler, final String timeout, final String deployLinpack, final String targetUrl, final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled, final String pathToSource) throws SeMoDeException {
        try {
            this.userConfig.getCalibrationConfig().getAwsCalibrationConfig().update(region, runtime, awsArnRole, functionHandler, timeout, deployLinpack, targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled, pathToSource);
        } catch (final IOException e) {
            throw new SeMoDeException("Error during memory Size parsing");
        }
    }

    public void updateLocalConfig(final String localSteps, final String numberOfLocalCalibrations, final String enabled, final String dockerSourceFolder) {
        this.userConfig.getCalibrationConfig().getLocalConfig().update(localSteps, numberOfLocalCalibrations, enabled, dockerSourceFolder);
    }

    public AWSCalibrationConfig getAWSConfig() {
        return this.userConfig.getCalibrationConfig().getAwsCalibrationConfig();
    }

    public LocalCalibrationConfig getLocalConfig() {
        return this.userConfig.getCalibrationConfig().getLocalConfig();
    }

    public boolean isLocalEnabled() {
        return this.userConfig.getCalibrationConfig().getLocalConfig().isLocalEnabled();
    }

    public boolean isAWSEnabled() {
        return this.userConfig.getCalibrationConfig().getAwsCalibrationConfig().enabled;
    }

    public double getLocalSteps() {
        return this.userConfig.getCalibrationConfig().getLocalConfig().getLocalSteps();
    }

    /**
     * Load the user config from file.
     *
     * @return
     * @throws SeMoDeException
     */
    public void loadUserConfig(final String path) throws SeMoDeException {
        final ObjectMapper om = new ObjectMapper();
        try {
            this.userConfig = om.readValue(Paths.get(path).toFile(), UserConfig.class);
        } catch (final IOException e) {
            throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
        }
    }

    public void initializeCalibrationFromGlobal(final GlobalConfig globalConfig) {
        this.userConfig.setCalibrationConfig(new CalibrationConfig(globalConfig.getCalibrationConfig()));
    }

    public void saveUserConfigToFile(final Path pathToConfig) throws SeMoDeException {
        try {
            new ObjectMapper().writer().withDefaultPrettyPrinter().writeValue(pathToConfig.toFile(),
                    this.userConfig);
        } catch (final IOException e) {
            throw new SeMoDeException("Configuration could not be saved.", e);
        }
    }

    public String getPrintableString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.userConfig);
    }

    public void updateAWSFunctionBenchmarkConfig(final String region, final String runtime, final String awsArnRole, final String functionHandler,
                                                 final String timeout, final String memorySizes, final String pathToSource, final String targetUrl,
                                                 final String apiKey) throws SeMoDeException {
        try {
            this.userConfig.getBenchmarkConfig().getAwsBenchmarkConfig().getFunctionConfig().update(region, runtime, awsArnRole, functionHandler, timeout, targetUrl, apiKey, memorySizes, pathToSource);
        } catch (final IOException e) {
            throw new SeMoDeException("Error during memory Size parsing");
        }
    }

    /**
     * Creates a list of all benchmark configs, which are enabled.
     * Currently only AWS is enabled.
     *
     * @param setupName
     * @return
     */
    public List<BenchmarkMethods> createBenchmarkMethodsFromConfig(final String setupName) throws SeMoDeException {
        final List<BenchmarkMethods> benchmarkMethods = new ArrayList<>();
        // if this is the case, the benchmark config was initialized, see function above
        if (this.userConfig.getBenchmarkConfig().awsBenchmarkConfig != null) {
            benchmarkMethods.add(new AWSBenchmark(setupName, this.userConfig.getBenchmarkConfig().awsBenchmarkConfig));
        }
        return benchmarkMethods;
    }

    public void updateGlobalBenchmarkParameters(final String numberOfThreads, final String benchmarkingMode, final String benchmarkingParameters, final String postArgument) {
        this.userConfig.getBenchmarkConfig().update(numberOfThreads, benchmarkingMode, benchmarkingParameters, postArgument);
    }

    public BenchmarkConfig getBenchmarkConfig() {
        return this.userConfig.getBenchmarkConfig();
    }

    public void logBenchmarkStartTime() {
        this.userConfig.getBenchmarkConfig().logBenchmarkStartTime();
    }

    public void logBenchmarkEndTime() {
        this.userConfig.getBenchmarkConfig().logBenchmarkEndTime();
    }

    public Pair<LocalDateTime, LocalDateTime> getStartAndEndTime() throws SeMoDeException {
        try {
            return new ImmutablePair<>(LocalDateTime.parse(this.userConfig.getBenchmarkConfig().startTime),
                    LocalDateTime.parse(this.userConfig.getBenchmarkConfig().endTime));
        } catch (final DateTimeParseException e) {
            throw new SeMoDeException("Start or end time not parsable: start: " + this.userConfig.getBenchmarkConfig().startTime
                    + " end: " + this.userConfig.getBenchmarkConfig().endTime);
        }
    }

    public void updateMappingConfig(final String localCalibrationFile, final String providerCalibrationFile, final String memoryJSON) throws SeMoDeException {
        this.userConfig.getCalibrationConfig().getMappingCalibrationConfig().update(localCalibrationFile, providerCalibrationFile, memoryJSON);
    }

    public MappingCalibrationConfig getMappingConfig() {
        return this.userConfig.getCalibrationConfig().getMappingCalibrationConfig();
    }
}
