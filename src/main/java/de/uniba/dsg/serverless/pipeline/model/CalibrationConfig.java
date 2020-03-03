package de.uniba.dsg.serverless.pipeline.model;

import com.google.common.primitives.Doubles;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibrationConfig;

/**
 * Model class for calibration config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
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
