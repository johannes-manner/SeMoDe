package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.calibration.aws.AWSCalibrationConfig;

public class CalibrationConfig {

    // local parameter
    private double localSteps;
    private boolean localEnabled;
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
        this.localSteps = calibrationConfig.localSteps;
        this.awsConfig = new AWSCalibrationConfig(calibrationConfig.awsConfig);
    }

    public double getLocalSteps() {
        return this.localSteps;
    }

    public void setLocalSteps(final double localSteps) {
        this.localSteps = localSteps;
    }

    public AWSCalibrationConfig getAwsConfig() {
        return this.awsConfig;
    }

    public void setAwsConfig(final AWSCalibrationConfig awsConfig) {
        this.awsConfig = awsConfig;
    }

    public boolean isLocalEnabled() {
        return this.localEnabled;
    }

    public void setLocalEnabled(final boolean localEnabled) {
        this.localEnabled = localEnabled;
    }

    @Override
    public String toString() {
        return "CalibrationConfig{" +
                "localSteps=" + this.localSteps +
                ", localEnabled=" + this.localEnabled +
                ", awsConfig=" + this.awsConfig +
                '}';
    }
}
