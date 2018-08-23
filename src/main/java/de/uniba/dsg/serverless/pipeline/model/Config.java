package de.uniba.dsg.serverless.pipeline.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

	private List<ProviderConfig> providerConfigs;
	private List<LanguageConfig> languageConfigs;

	public Config() {
	}

	public List<ProviderConfig> getProviderConfigs() {
		return providerConfigs;
	}
	
	public Map<String, ProviderConfig> getProviderConfigMap(){
		Map<String, ProviderConfig> providerMap = new HashMap<>();
		for(ProviderConfig provider : providerConfigs) {
			providerMap.put(provider.getName(), provider);
		}
		return providerMap;
	}
	
	public Map<String, LanguageConfig> getLanguageConfigMap(){
		Map<String, LanguageConfig> languageMap = new HashMap<>();
		for(LanguageConfig language : languageConfigs) {
			languageMap.put(language.getIdentifier(), language);
		}
		return languageMap;
	}

	public void setProviderConfigs(List<ProviderConfig> providerConfigs) {
		this.providerConfigs = providerConfigs;
	}

	public List<LanguageConfig> getLanguageConfigs() {
		return languageConfigs;
	}

	public void setLanguageConfigs(List<LanguageConfig> languageConfigs) {
		this.languageConfigs = languageConfigs;
	}

	@Override
	public String toString() {
		return "Config [providerConfigs=" + providerConfigs + ", languageConfigs=" + languageConfigs + "]";
	}
}
