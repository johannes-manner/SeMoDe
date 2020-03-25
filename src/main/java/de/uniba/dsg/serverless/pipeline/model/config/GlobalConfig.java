package de.uniba.dsg.serverless.pipeline.model.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalConfig {

    private List<LanguageConfig> languageConfigs;
    private CalibrationConfig calibrationConfig;

    public GlobalConfig() {
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
                ", languageConfigs=" + this.languageConfigs +
                ", calibrationConfig=" + this.calibrationConfig +
                '}';
    }
}
