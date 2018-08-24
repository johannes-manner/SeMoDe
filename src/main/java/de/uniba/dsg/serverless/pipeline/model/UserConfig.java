package de.uniba.dsg.serverless.pipeline.model;

import java.util.List;

public class UserConfig {

	private List<ProviderConfig> providerConfigs;
	private BenchmarkConfig benchmarkConfig;
	
	public UserConfig() {
	}

	public UserConfig(List<ProviderConfig> providerConfigs, BenchmarkConfig benchmarkConfig) {
		super();
		this.providerConfigs = providerConfigs;
		this.benchmarkConfig = benchmarkConfig;
	}
	public List<ProviderConfig> getProviderConfigs() {
		return providerConfigs;
	}

	public void setProviderConfigs(List<ProviderConfig> providerConfigs) {
		this.providerConfigs = providerConfigs;
	}

	public BenchmarkConfig getBenchmarkConfig() {
		return benchmarkConfig;
	}

	public void setBenchmarkConfig(BenchmarkConfig benchmarkConfig) {
		this.benchmarkConfig = benchmarkConfig;
	}

	@Override
	public String toString() {
		return "UserConfig [providerConfigs=" + providerConfigs + ", benchmarkConfig=" + benchmarkConfig + "]";
	}

}
