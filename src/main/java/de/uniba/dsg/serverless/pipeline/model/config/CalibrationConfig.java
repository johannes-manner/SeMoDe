package de.uniba.dsg.serverless.pipeline.model.config;

import de.uniba.dsg.serverless.calibration.local.LocalCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;

/**
 * Model class for calibration config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class CalibrationConfig {

    // local parameter
    private LocalCalibrationConfig localConfig;
    // aws parameter
    private AWSCalibrationConfig awsCalibrationConfig;

    // for mappping
    private MappingCalibrationConfig mappingCalibrationConfig;

    // for executing
    private RunningCalibrationConfig runningCalibrationConfig;

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
        this.awsCalibrationConfig = new AWSCalibrationConfig(calibrationConfig.awsCalibrationConfig);
        this.mappingCalibrationConfig = new MappingCalibrationConfig(calibrationConfig.mappingCalibrationConfig);
    }

    public LocalCalibrationConfig getLocalConfig() {
        return this.localConfig;
    }

    public void setLocalConfig(final LocalCalibrationConfig localConfig) {
        this.localConfig = localConfig;
    }

    public AWSCalibrationConfig getAwsCalibrationConfig() {
        return this.awsCalibrationConfig;
    }

    public void setAwsCalibrationConfig(final AWSCalibrationConfig awsCalibrationConfig) {
        this.awsCalibrationConfig = awsCalibrationConfig;
    }

    public MappingCalibrationConfig getMappingCalibrationConfig() {
        if (this.mappingCalibrationConfig == null) {
            this.mappingCalibrationConfig = new MappingCalibrationConfig();
        }
        return this.mappingCalibrationConfig;
    }

    public void setMappingCalibrationConfig(final MappingCalibrationConfig mappingCalibrationConfig) {
        this.mappingCalibrationConfig = mappingCalibrationConfig;
    }

    @Override
    public String toString() {
        return "CalibrationConfig{" +
                "localConfig=" + this.localConfig +
                ", awsCalibrationConfig=" + this.awsCalibrationConfig +
                ", mappingCalibrationConfig=" + this.mappingCalibrationConfig +
                '}';
    }

    public RunningCalibrationConfig getRunningCalibrationConfig() {
        if (this.runningCalibrationConfig == null) {
            this.runningCalibrationConfig = new RunningCalibrationConfig();
        }
        return this.runningCalibrationConfig;
    }
}
