package de.uniba.dsg.serverless.pipeline.model.config;

public class GlobalConfig {

    private CalibrationConfig calibrationConfig;

    public GlobalConfig() {
    }

    public CalibrationConfig getCalibrationConfig() {
        return this.calibrationConfig;
    }

    public void setCalibrationConfig(final CalibrationConfig calibrationConfig) {
        this.calibrationConfig = calibrationConfig;
    }

    @Override
    public String toString() {
        return "Config{" +
                ", calibrationConfig=" + this.calibrationConfig +
                '}';
    }
}
