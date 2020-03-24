package de.uniba.dsg.serverless.pipeline.model.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for user config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class UserConfig {

    private List<ProviderConfig> providerConfigs;
    private BenchmarkConfig benchmarkConfig;
    private CalibrationConfig calibrationConfig;

    public UserConfig() {
    }

    /**
     * providerConfigs is not initialized with a default value.
     * Throws a NullPointerException when loading a wrong setup.
     *
     * @return
     */
    public List<ProviderConfig> getProviderConfigs() {
        if (this.providerConfigs == null) {
            this.providerConfigs = new ArrayList<>();
        }
        return this.providerConfigs;
    }

    public void setProviderConfigs(final List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    /**
     * benchmarkConfig is not initialized with a default value.
     * Throws a NullPointerException when loading a wrong setup.
     *
     * @return
     */
    public BenchmarkConfig getBenchmarkConfig() {
        if (this.benchmarkConfig == null) {
            this.benchmarkConfig = new BenchmarkConfig();
        }
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
}