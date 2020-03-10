package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.calibration.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.calibration.local.LocalCalibrationConfig;

/**
 * Model class for calibration config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class CalibrationConfig {

    // local parameter
    private LocalCalibrationConfig localConfig;
    // aws parameter
    private AWSCalibrationConfig awsConfig;

    public CalibrationConfig() {
    }

    /**
     * Copy the calibration config (especially used when initializing the pipeline).
     * Necessary to validate the values and work on copied values.
     *
     * @param calibrationConfig
     */
    public CalibrationConfig(final CalibrationConfig calibrationConfig) {
        this.localConfig = new LocalCalibrationConfig(calibrationConfig.localConfig);
        this.awsConfig = new AWSCalibrationConfig(calibrationConfig.awsConfig);
    }

    public LocalCalibrationConfig getLocalConfig() {
        return this.localConfig;
    }

    public void setLocalConfig(final LocalCalibrationConfig localConfig) {
        this.localConfig = localConfig;
    }

    public AWSCalibrationConfig getAwsConfig() {
        return this.awsConfig;
    }

    public void setAwsConfig(final AWSCalibrationConfig awsConfig) {
        this.awsConfig = awsConfig;
    }

    @Override
    public String toString() {
        return "CalibrationConfig{" +
                "localCalibrationConfig=" + this.localConfig +
                ", awsConfig=" + this.awsConfig +
                '}';
    }
}
