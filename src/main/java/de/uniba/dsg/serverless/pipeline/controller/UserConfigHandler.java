package de.uniba.dsg.serverless.pipeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import de.uniba.dsg.serverless.calibration.local.LocalCalibrationConfig;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.*;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    public void updateLocalConfig(final String localSteps, final String numberOfLocalCalibrations, final String enabled) {
        this.userConfig.getCalibrationConfig().getLocalConfig().update(localSteps, numberOfLocalCalibrations, enabled);
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

    public void addOrChangeProviderConfig(final Map<String, ProviderConfig> providerConfigMap, final String provider, final String memorySize, final String language, final String deploymentSize) throws IOException, SeMoDeException {
        boolean providerMissingInList = true;

        for (final ProviderConfig providerConfig : this.userConfig.getProviderConfigs()) {
            if (providerConfig.getName().equals(provider)) {
                providerConfig.validateAndUpdate(providerConfigMap, memorySize, language, deploymentSize);
                providerMissingInList = false;
                break;
            }
        }

        if (providerMissingInList) {
            final ProviderConfig config = new ProviderConfig();
            config.validateAndCreate(providerConfigMap, provider, memorySize, language, deploymentSize);
            this.userConfig.getProviderConfigs().add(config);
        }

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

    public void updateBenchmarkConfig(final BenchmarkConfig config) {
        this.userConfig.setBenchmarkConfig(config);
    }

    public Map<String, ProviderConfig> getUserConfigProviders() {
        final Map<String, ProviderConfig> temp = new HashMap<>();
        for (final ProviderConfig config : this.userConfig.getProviderConfigs()) {
            temp.put(config.getName(), config);
        }
        return temp;
    }
}
