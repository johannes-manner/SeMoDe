package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.calibration.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.UserConfig;

import java.io.IOException;

/**
 * Wrapper to change the attributes of the user config class.
 * If the changes are made directly in the model classes, there occur json parsing errors.
 */
public class UserConfigHandler {

    private final UserConfig userConfig;

    public UserConfigHandler(final UserConfig config) {
        this.userConfig = config;
    }

    public void updateAWSConfig(final String targetUrl, final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled) throws SeMoDeException {
        try {
            this.userConfig.getCalibrationConfig().getAwsConfig().update(targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled);
        } catch (final IOException e) {
            throw new SeMoDeException("Error during memory Size parsing");
        }
    }

    public void updateLocalConfig(final String localSteps, final String enabled) {
        this.userConfig.getCalibrationConfig().updateLocalConfig(localSteps, enabled);
    }

    public AWSCalibrationConfig getAWSConfig() {
        return this.userConfig.getCalibrationConfig().getAwsConfig();
    }

    public boolean isLocalEnabled() {
        return this.userConfig.getCalibrationConfig().isLocalEnabled();
    }

    public boolean isAWSEnabled() {
        return this.userConfig.getCalibrationConfig().getAwsConfig().enabled;
    }

    public double getLocalSteps() {
        return this.userConfig.getCalibrationConfig().getLocalSteps();
    }

}
