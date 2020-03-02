package de.uniba.dsg.serverless.pipeline.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private List<ProviderConfig> providerConfigs;
    private List<LanguageConfig> languageConfigs;
    private CalibrationConfig calibrationConfig;

    public Config() {
    }

    public List<ProviderConfig> getProviderConfigs() {
        return this.providerConfigs;
    }

    public void setProviderConfigs(final List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    public Map<String, ProviderConfig> getProviderConfigMap() {
        final Map<String, ProviderConfig> providerMap = new HashMap<>();
        for (final ProviderConfig provider : this.providerConfigs) {
            providerMap.put(provider.getName(), provider);
        }
        return providerMap;
    }

    public Map<String, LanguageConfig> getLanguageConfigMap() {
        final Map<String, LanguageConfig> languageMap = new HashMap<>();
        for (final LanguageConfig language : this.languageConfigs) {
            languageMap.put(language.getIdentifier(), language);
        }
        return languageMap;
    }

    public List<LanguageConfig> getLanguageConfigs() {
        return this.languageConfigs;
    }

    public void setLanguageConfigs(final List<LanguageConfig> languageConfigs) {
        this.languageConfigs = languageConfigs;
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
                "providerConfigs=" + this.providerConfigs +
                ", languageConfigs=" + this.languageConfigs +
                ", calibrationConfig=" + this.calibrationConfig +
                '}';
    }
}
