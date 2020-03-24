package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.benchmark.FunctionTrigger;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Deprecated
public class BenchmarkUtility extends CustomUtility {

    private static final Logger logger = LogManager.getLogger(BenchmarkUtility.class.getName());

    public BenchmarkUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {
        // remove the function name of the argument list
        // function name is necessary for log file distinction
        args.remove(0);
        try {
            final int failedRequests = this.executeBenchmark(args);
            logger.info("Number of failed requests: " + failedRequests);
        } catch (final SeMoDeException e) {
            logger.fatal("Exception during benchmark execution.", e);
            return;
        }

    }

    private void logUsage() {
        logger.fatal("Usage for each mode:\n"
                + "(Mode 1) PROVIDER_FUNCTION_NAME URL JSONINPUT concurrent NUMBER_OF_THREADS NUMBER_OF_REQUESTS\n"
                + "(Mode 2) PROVIDER_FUNCTION_NAME URL JSONINPUT sequentialInterval NUMBER_OF_THREADS NUMBER_OF_REQUESTS DELAY\n"
                + "(Mode 3) PROVIDER_FUNCTION_NAME URL JSONINPUT sequentailWait NUMBER_OF_THREADS NUMBER_OF_REQUESTS DELAY\n"
                + "(Mode 4) PROVIDER_FUNCTION_NAME URL JSONINPUT sequentialConcurrent NUMBER_OF_THREADS NUMBER_OF_GROUPS NUMBER_OF_REQUESTS_GROUP DELAY\n"
                + "(Mode 5) PROVIDER_FUNCTION_NAME URL JSONINPUT sequentialChangingInterval NUMBER_OF_THREADS NUMBER_OF_REQUESTS (DELAY)+"
                + "(Mode 6) PROVIDER_FUNCTION_NAME URL JSONINPUT sequentialChangingWait NUMBER_OF_THREADS NUMBER_OF_REQUESTS (DELAY)+"
                + "(Mode 7) PROVIDER_FUNCTION_NAME URL JSONINPUT arbitraryLoadPattern NUMBER_OF_THREADS FILE.csv");
    }

    public int executeBenchmark(final List<String> args) throws SeMoDeException {

//		this.validateArgumentSize(args, 5);
//
//        final LoadPatternGenerator loadpatternGenerator = new LoadPatternGenerator();
//        Path loadPatternFile = Paths.get("");
//        final URL url;
//        final String jsonInput;
//        final BenchmarkMode mode;
//        final int noThreads;
//
//        try {
//            url = new URL(args.get(0));
//            jsonInput = this.readJsonInput(args.get(1));
//            mode = BenchmarkMode.fromString(args.get(2));
//            noThreads = Integer.parseInt(args.get(3));
//
//            switch (mode) {
//                case CONCURRENT:
//					this.validateArgumentSize(args, 5);
//                    loadPatternFile = loadpatternGenerator.generateConcurrentLoadPattern(args);
//                    break;
//                case SEQUENTIAL_INTERVAL:
//					this.validateArgumentSize(args, 6);
//                    loadPatternFile = loadpatternGenerator.generateSequentialInterval(args);
//                    break;
//                case SEQUENTIAL_CONCURRENT:
//					this.validateArgumentSize(args, 7);
//                    loadPatternFile = loadpatternGenerator.generateSequentialConcurrent(args);
//                    break;
//                case SEQUENTIAL_CHANGING_INTERVAL:
//					this.validateArgumentSize(args, 6);
//                    loadPatternFile = loadpatternGenerator.generateSequentialChangingInterval(args);
//                    break;
//                case ARBITRARY_LOAD_PATTERN:
//					this.validateArgumentSize(args, 5);
//                    loadPatternFile = Paths.get(args.get(4));
//                    break;
//                default:
//                    this.logUsage();
//                    throw new SeMoDeException("Mode " + mode + " is not implemented.");
//            }
//        } catch (final NumberFormatException e) {
//            throw new SeMoDeException(e.getMessage(), e);
//        } catch (final MalformedURLException e) {
//            throw new SeMoDeException("Malformed URL " + args.get(0), e);
//        } catch (final IOException | InvalidPathException e) {
//            throw new SeMoDeException("Exception while reading the json from the file " + args.get(1), e);
//        }
//
//        return this.executeBenchmark(loadPatternFile, jsonInput, url, noThreads);
        return 0;
    }

    private int executeBenchmark(final Path loadPatternFile, final String jsonInput, final URL url, final int noThreads) throws SeMoDeException {

        // TODO think about a more sophisticated way to compute number of threads
        final ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(noThreads);
        final List<Double> timestamps;
        final List<Future<String>> responses = new ArrayList<>();
        int failedRequests = 0;

        try {
            timestamps = Files.readAllLines(loadPatternFile).stream().map(s -> Double.parseDouble(s))
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            throw new SeMoDeException("Load pattern file was not readable", e);
        }

        for (final double d : timestamps) {
            responses.add(
                    executor.schedule(new FunctionTrigger(jsonInput, url), (long) (d * 1000), TimeUnit.MILLISECONDS));
        }

        for (final Future<String> future : responses) {
            failedRequests = this.exceptionHandlingFuture(executor, failedRequests, future);
        }

        this.shutdownExecutorAndAwaitTermination(executor, 0);

        return failedRequests;
    }

    private void validateArgumentSize(final List<String> args, final int size) throws SeMoDeException {
        if (args.size() < size) {
            throw new SeMoDeException("Number of arguments invalid.");
        }
    }

    private String readJsonInput(final String path) throws IOException, InvalidPathException {
        final List<String> lines = Files.readAllLines(Paths.get(path));
        return lines.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    private void shutdownExecutorAndAwaitTermination(final ExecutorService executorService, final int maxWaitTime) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(maxWaitTime, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private int exceptionHandlingFuture(final ExecutorService executorService, int failedRequests, final Future<String> future)
            throws SeMoDeException {
        try {
            do {
                try {
                    future.get();
                } catch (final InterruptedException e) {
                    logger.info("InterruptedException - investigate this orphan exception");
                }
            } while (!future.isDone());
        } catch (final CancellationException | ExecutionException e) {
            logger.warn("ExecutionException", e);
            failedRequests++;
        }
        return failedRequests;
    }

}
