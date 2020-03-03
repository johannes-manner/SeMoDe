package de.uniba.dsg.serverless.pipeline.model;

import com.google.common.primitives.Doubles;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibrationConfig;

import java.io.IOException;

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

    public void updateAWSConfig(final String targetUrl, final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled) throws IOException {
        this.awsConfig.update(targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled);
    }

    public void updateLocalConfig(final String steps, final String enabled) {
        if (!"".equals(steps) && Doubles.tryParse(steps) != null) {
            this.localSteps = Doubles.tryParse(steps);
        }
        if (!"".equals(enabled)) {
            // returns only true, if enabled is "true", otherwise false (also for incorrect inputs or null)
            this.localEnabled = Boolean.parseBoolean(enabled);
        }
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
