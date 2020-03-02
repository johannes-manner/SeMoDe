package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.util.List;

public class UserConfig {

    private List<ProviderConfig> providerConfigs;
    private BenchmarkConfig benchmarkConfig;
    private CalibrationConfig calibrationConfig;

    public UserConfig() {
    }

    public UserConfig(final List<ProviderConfig> providerConfigs, final BenchmarkConfig benchmarkConfig, final CalibrationConfig calibrationConfig) {
        super();
        this.providerConfigs = providerConfigs;
        this.benchmarkConfig = benchmarkConfig;
        this.calibrationConfig = calibrationConfig;
    }

    public List<ProviderConfig> getProviderConfigs() {
        return this.providerConfigs;
    }

    public void setProviderConfigs(final List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    public BenchmarkConfig getBenchmarkConfig() {
        return this.benchmarkConfig;
    }

    public void setBenchmarkConfig(final BenchmarkConfig benchmarkConfig) {
        this.benchmarkConfig = benchmarkConfig;
    }

    public CalibrationConfig getCalibrationConfig() {
        return this.calibrationConfig;
    }

    public void setCalibrationConfig(final CalibrationConfig calibrationConfig) {
        this.calibrationConfig = calibrationConfig;
    }

    @Override
    public String toString() {
        return "{" +
                "providerConfigs=" + this.providerConfigs +
                ", benchmarkConfig=" + this.benchmarkConfig +
                ", calibrationConfig=" + this.calibrationConfig +
                '}';
    }

    public void updateLocalSteps(final Double localSteps) {
        this.getCalibrationConfig().setLocalSteps(localSteps);
    }

    public void updateAWSConfig(final String targetUrl, final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions, final String enabled) throws SeMoDeException {
        try {
            this.getCalibrationConfig().getAwsConfig().update(targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled);
        } catch (final IOException e) {
            throw new SeMoDeException("Error during memory Size parsing");
        }
    }
}
