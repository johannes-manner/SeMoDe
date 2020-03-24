package de.uniba.dsg.serverless.benchmark;

import com.google.common.primitives.Doubles;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BenchmarkExecutor {

    private final Path pathToBenchmarkExecution;
    private final BenchmarkConfig benchmarkConfig;

    private Path loadPatternFile;

    public BenchmarkExecutor(final Path pathToBenchmarkExecution, final BenchmarkConfig benchmarkConfig) {
        this.pathToBenchmarkExecution = pathToBenchmarkExecution;
        this.benchmarkConfig = benchmarkConfig;
    }

    public void generateLoadPattern() throws SeMoDeException {
        this.loadPatternFile = this.pathToBenchmarkExecution.resolve("loadPattern.csv");
        final LoadPatternGenerator loadpatternGenerator = new LoadPatternGenerator(this.loadPatternFile);

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
                loadpatternGenerator.copyArbitraryLoadPattern(this.benchmarkConfig.benchmarkParameters);
                break;
        }
    }

    /**
     * Currently only aws is supported - for next provider integration, rethink the architecture.
     *
     * @param benchmarkMethodsFromConfig
     */
    public void executeBenchmark(final List<BenchmarkMethods> benchmarkMethodsFromConfig) throws SeMoDeException {

        final List<Double> timestamps = this.loadLoadPatternFromFile();

        // TODO think about a more sophisticated way to compute number of threads
        // TODO number of threads really needed?
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        final List<Future<String>> responses = new ArrayList<>();


        // for each provider
        for (final BenchmarkMethods benchmarkMethods : benchmarkMethodsFromConfig) {
            if (benchmarkMethods.isInitialized()) {
                // for each function
                for (final String functionEndpoint : benchmarkMethods.getUrlEndpointsOnPlatform()) {
                    // for each timestamp
                    for (final double timestamp : timestamps) {
                        try {
                            // 1 second time before the processing starts to get the processing of the functions triggers done
                            responses.add(executor.schedule(new FunctionTrigger(this.benchmarkConfig.postArgument, new URL(functionEndpoint)), (long) (1000 + timestamp * 1000), TimeUnit.MILLISECONDS));
                        } catch (final MalformedURLException e) {
                            throw new SeMoDeException("URL was malformed: " + functionEndpoint, e);
                        }
                    }
                }
            }
        }

        // TODO shutdown executor service
    }

    private List<Double> loadLoadPatternFromFile() throws SeMoDeException {
        final List<Double> timestamps;
        try {
            timestamps = Files.readAllLines(this.loadPatternFile).stream().map(Doubles::tryParse).collect(Collectors.toList());
        } catch (final IOException e) {
            throw new SeMoDeException("Load pattern file was not readable", e);
        }
        return timestamps;
    }
}
