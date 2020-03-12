package de.uniba.dsg.serverless.pipeline.model;

/**
 * Model class for benchmark execution config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class BenchmarkConfig {

    // corePoolSize for executor service
    private String numberThreads;
    private String benchmarkMode;
    private String benchmarkParameters;

    public BenchmarkConfig() {
    }

    public BenchmarkConfig(final String numberOfThreads, final String benchmarkMode, final String benchmarkParameters) {
        super();
        this.numberThreads = numberOfThreads;
        this.benchmarkMode = benchmarkMode;
        this.benchmarkParameters = benchmarkParameters;
    }

    public String getNumberThreads() {
        return this.numberThreads;
    }

    public void setNumberThreads(final String numberThreads) {
        this.numberThreads = numberThreads;
    }

    public String getBenchmarkMode() {
        return this.benchmarkMode;
    }

    public void setBenchmarkMode(final String benchmarkMode) {
        this.benchmarkMode = benchmarkMode;
    }

    public String getBenchmarkParameters() {
        return this.benchmarkParameters;
    }

    public void setBenchmarkParameters(final String benchmarkParameters) {
        this.benchmarkParameters = benchmarkParameters;
    }

    @Override
    public String toString() {
        return "BenchmarkConfig [numberThreads=" + this.numberThreads + ", benchmarkMode=" + this.benchmarkMode
                + ", benchmarkParameters=" + this.benchmarkParameters + "]";
    }

}
