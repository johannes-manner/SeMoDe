package de.uniba.dsg.serverless.pipeline.model;

public class BenchmarkConfig {

	private String benchmarkMode;
	private String benchmarkParameters;

	public BenchmarkConfig() {
	}

	public BenchmarkConfig(String benchmarkMode, String benchmarkParameters) {
		super();
		this.benchmarkMode = benchmarkMode;
		this.benchmarkParameters = benchmarkParameters;
	}

	public String getBenchmarkMode() {
		return benchmarkMode;
	}

	public void setBenchmarkMode(String benchmarkMode) {
		this.benchmarkMode = benchmarkMode;
	}

	public String getBenchmarkParameters() {
		return benchmarkParameters;
	}

	public void setBenchmarkParameters(String benchmarkParameters) {
		this.benchmarkParameters = benchmarkParameters;
	}

}
