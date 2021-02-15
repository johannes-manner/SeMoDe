package de.uniba.dsg.serverless.pipeline.benchmark;

import com.google.common.primitives.Doubles;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.benchmark.model.FunctionTrigger;
import de.uniba.dsg.serverless.pipeline.benchmark.model.FunctionTriggerWrapper;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.util.LoadPatternGenerator;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
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

        String benchmarkMode = this.benchmarkConfig.getBenchmarkMode();
        String benchmarkParameters = this.benchmarkConfig.getBenchmarkParameters();
        if (benchmarkMode.equals(BenchmarkMode.CONCURRENT)) {
            loadpatternGenerator.generateConcurrentLoadPattern(benchmarkParameters);
        } else if (benchmarkMode.equals(BenchmarkMode.SEQUENTIAL_INTERVAL)) {
            loadpatternGenerator.generateSequentialInterval(benchmarkParameters);
        } else if (benchmarkMode.equals(BenchmarkMode.SEQUENTIAL_CONCURRENT)) {
            loadpatternGenerator.generateSequentialConcurrent(benchmarkParameters);
        } else if (benchmarkMode.equals(BenchmarkMode.SEQUENTIAL_CHANGING_INTERVAL)) {
            loadpatternGenerator.generateSequentialChangingInterval(benchmarkParameters);
        } else if (benchmarkMode.equals(BenchmarkMode.ARBITRARY_LOAD_PATTERN)) {
            loadpatternGenerator.copyArbitraryLoadPattern(benchmarkParameters);
        } else {
            throw new SeMoDeException("Mode is unknown. Entered mode = " + benchmarkMode);
        }
    }

    /**
     * Currently only aws is supported - for next provider integration, rethink the architecture.
     * <br/>
     * There was an ongoing discussion, if the scheduled executor service needs the number of the core pool size configured.
     * Since the invocations to the cloud functions are synchronous to enable a proper measurement of the duration from
     * a client perspective, the decision is now to use another executor service, a so called <i>delegator</i>, which
     * gets a wrapped function trigger and executes it. This solves the problem to configure the number of threads for the scheduled thread pool.
     */
    public List<LocalRESTEvent> executeBenchmark(final List<BenchmarkMethods> benchmarkMethodsFromConfig) throws SeMoDeException {

        final List<Double> timestamps = this.loadLoadPatternFromFile();

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        final ExecutorService delegator = Executors.newCachedThreadPool();
        final List<Future<LocalRESTEvent>> responses = new CopyOnWriteArrayList<>();

        long tmpTimestamp = 0;
        // for each provider
        for (final BenchmarkMethods benchmarkMethods : benchmarkMethodsFromConfig) {
            if (benchmarkMethods.isInitialized()) {
                // for each function
                final Map<String, String> headerParameters = benchmarkMethods.getHeaderParameter();
                for (final String functionEndpoint : benchmarkMethods.getUrlEndpointsOnPlatform()) {
                    // for each timestamp
                    for (final double timestamp : timestamps) {
                        try {
                            // 1 second time before the processing starts to get the processing of the functions triggers done
                            tmpTimestamp = (long) (1000 + timestamp * 1000);
                            final FunctionTrigger f = new FunctionTrigger(benchmarkMethods.getPlatform(), this.benchmarkConfig.getPostArgument(), new URL(functionEndpoint), headerParameters);
                            FunctionTriggerWrapper fWrapper = new FunctionTriggerWrapper(delegator, responses, f);
                            executor.schedule(fWrapper, tmpTimestamp, TimeUnit.MILLISECONDS);
                        } catch (final MalformedURLException e) {
                            throw new SeMoDeException("URL was malformed: " + functionEndpoint, e);
                        }
                    }
                }
            }
        }

        // shut down the first scheduled service, means that all wrapper function trigger tasks are run and the
        // function trigger tasks are submitted
        // time to wait is the last timestamp from now on executing a function
        this.shutdownExecService(executor, tmpTimestamp + 30_000);
        // wait for the function trigger tasks to terminate
        this.shutdownExecService(delegator, tmpTimestamp + 300_000);


        // get is made after the executor services are shutdown correctly, otherwise results from the platform
        // may not be computed - we use the 300 seconds timeout to wait, see the shutdown of the delegator
        List<LocalRESTEvent> events = new ArrayList<>();
        for (Future<LocalRESTEvent> futureEvent : responses) {
            try {
                events.add(futureEvent.get());
            } catch (InterruptedException e) {
                // do not use interruption mechanism for termination
            } catch (ExecutionException e) {
                throw new SeMoDeException(e);
            }
        }
        return events;
    }

    private void shutdownExecService(ExecutorService executor, long timeToWaitInMS) throws SeMoDeException {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeToWaitInMS, TimeUnit.MILLISECONDS)) {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    throw new SeMoDeException("Pool did not terminate. Shutdown JVM manually!");
                }
            }
        } catch (final InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Shutdown executor service successfully");
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
