package de.uniba.dsg.serverless.pipeline.model;

public class BenchmarkConfig {

	// corePoolSize for executor service
	private String numberThreads;
	private String benchmarkMode;
	private String benchmarkParameters;

	public BenchmarkConfig() {
	}

	public BenchmarkConfig(String numberOfThreads, String benchmarkMode, String benchmarkParameters) {
		super();
		this.numberThreads = numberOfThreads;
		this.benchmarkMode = benchmarkMode;
		this.benchmarkParameters = benchmarkParameters;
	}

	public String getNumberThreads() {
		return numberThreads;
	}

	public void setNumberThreads(String numberThreads) {
		this.numberThreads = numberThreads;
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

	@Override
	public String toString() {
		return "BenchmarkConfig [numberThreads=" + numberThreads + ", benchmarkMode=" + benchmarkMode
				+ ", benchmarkParameters=" + benchmarkParameters + "]";
	}

}
