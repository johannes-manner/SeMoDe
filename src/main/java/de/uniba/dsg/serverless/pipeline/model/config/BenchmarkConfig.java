package de.uniba.dsg.serverless.pipeline.model.config;

import com.google.gson.annotations.Expose;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;

/**
 * Model class for benchmark execution config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class BenchmarkConfig {

    // corePoolSize for executor service
    @Expose
    public String numberThreads;
    @Expose
    public String benchmarkMode;
    @Expose
    public String benchmarkParameters;

    // aws parameters
    @Expose
    public AWSBenchmarkConfig awsBenchmarkConfig;

    public BenchmarkConfig() {
    }

    public BenchmarkConfig(final String numberOfThreads, final String benchmarkMode, final String benchmarkParameters) {
        super();
        this.numberThreads = numberOfThreads;
        this.benchmarkMode = benchmarkMode;
        this.benchmarkParameters = benchmarkParameters;
    }

    public AWSBenchmarkConfig getAwsBenchmarkConfig() {
        if (this.awsBenchmarkConfig == null) {
            this.awsBenchmarkConfig = new AWSBenchmarkConfig();
        }
        return this.awsBenchmarkConfig;
    }

    @Override
    public String toString() {
        return "BenchmarkConfig{" +
                "numberThreads='" + this.numberThreads + '\'' +
                ", benchmarkMode='" + this.benchmarkMode + '\'' +
                ", benchmarkParameters='" + this.benchmarkParameters + '\'' +
                ", awsBenchmarkConfig=" + this.awsBenchmarkConfig +
                '}';
    }
}
