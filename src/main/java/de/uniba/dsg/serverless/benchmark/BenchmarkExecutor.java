package de.uniba.dsg.serverless.benchmark;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BenchmarkExecutor {

    private final Path pathToBenchmarkExecution;
    private final BenchmarkConfig benchmarkConfig;

    public BenchmarkExecutor(final Path pathToBenchmarkExecution, final BenchmarkConfig benchmarkConfig) {
        this.pathToBenchmarkExecution = pathToBenchmarkExecution;
        this.benchmarkConfig = benchmarkConfig;
    }

    public void generateLoadPattern() throws SeMoDeException {
        final Path loadPatternFile = this.pathToBenchmarkExecution.resolve("loadPattern.csv");
        final LoadPatternGenerator loadpatternGenerator = new LoadPatternGenerator(loadPatternFile);

        switch (BenchmarkMode.fromString(this.benchmarkConfig.benchmarkMode)) {
            case CONCURRENT:
                loadpatternGenerator.generateConcurrentLoadPattern(this.benchmarkConfig.benchmarkParameters);
                break;
            case SEQUENTIAL_INTERVAL:
                loadpatternGenerator.generateSequentialInterval(this.benchmarkConfig.benchmarkParameters);
                break;
            case SEQUENTIAL_CONCURRENT:
                loadpatternGenerator.generateSequentialConcurrent(this.benchmarkConfig.benchmarkParameters);
                break;
            case SEQUENTIAL_CHANGING_INTERVAL:
                loadpatternGenerator.generateSequentialChangingInterval(this.benchmarkConfig.benchmarkParameters);
                break;
            case ARBITRARY_LOAD_PATTERN:
                Paths.get(this.benchmarkConfig.benchmarkParameters);
                break;
        }
    }

    // execute - > aws initialized
}
