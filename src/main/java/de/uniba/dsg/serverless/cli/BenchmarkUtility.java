package de.uniba.dsg.serverless.cli;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.benchmark.BenchmarkMode;
import de.uniba.dsg.serverless.benchmark.FunctionTrigger;
import de.uniba.dsg.serverless.benchmark.LoadPatternGenerator;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(BenchmarkUtility.class.getName());

	public BenchmarkUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		// remove the function name of the argument list
		// function name is necessary for log file distinction
		args.remove(0);
		try {
			int failedRequests = executeBenchmark(args);
			logger.info("Number of failed requests: " + failedRequests);
		} catch (SeMoDeException e) {
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

	public int executeBenchmark(List<String> args) throws SeMoDeException {

		validateArgumentSize(args, 5);

		final LoadPatternGenerator loadpatternGenerator = new LoadPatternGenerator();
		Path loadPatternFile = Paths.get("");
		URL url;
		String jsonInput;
		BenchmarkMode mode;
		int noThreads;

		try {
			url = new URL(args.get(0));
			jsonInput = this.readJsonInput(args.get(1));
			mode = BenchmarkMode.fromString(args.get(2));
			noThreads = Integer.parseInt(args.get(3));

			switch (mode) {
			case CONCURRENT:
				validateArgumentSize(args, 5);
				loadPatternFile = loadpatternGenerator.generateConcurrentLoadPattern(args);
				break;
			case SEQUENTIAL_INTERVAL:
				validateArgumentSize(args, 6);
				loadPatternFile = loadpatternGenerator.generateSequentialInterval(args);
				break;
			case SEQUENTIAL_CONCURRENT:
				validateArgumentSize(args, 7);
				loadPatternFile = loadpatternGenerator.generateSequentialConcurrent(args);
				break;
			case SEQUENTIAL_CHANGING_INTERVAL:
				validateArgumentSize(args, 6);
				loadPatternFile = loadpatternGenerator.generateSequentialChangingInterval(args);
				break;
			case ARBITRARY_LOAD_PATTERN:
				validateArgumentSize(args, 5);
				loadPatternFile = Paths.get(args.get(4));
				break;
			default:
				this.logUsage();
				throw new SeMoDeException("Mode " + mode + " is not implemented.");
			}
		} catch (NumberFormatException e) {
			throw new SeMoDeException(e.getMessage(), e);
		} catch (MalformedURLException e) {
			throw new SeMoDeException("Malformed URL " + args.get(0), e);
		} catch (IOException | InvalidPathException e) {
			throw new SeMoDeException("Exception while reading the json from the file " + args.get(1), e);
		}

		return this.executeBenchmark(loadPatternFile, jsonInput, url, noThreads);
	}

	private int executeBenchmark(Path loadPatternFile, String jsonInput, URL url, int noThreads) throws SeMoDeException {

		// TODO think about a more sophisticated way to compute number of threads
		ScheduledExecutorService executor = Executors
				.newScheduledThreadPool(noThreads);
		List<Double> timestamps;
		List<Future<String>> responses = new ArrayList<>();
		int failedRequests = 0;

		try {
			timestamps = Files.readAllLines(loadPatternFile).stream().map(s -> Double.parseDouble(s))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new SeMoDeException("Load pattern file was not readable", e);
		}

		for (double d : timestamps) {
			responses.add(
					executor.schedule(new FunctionTrigger(jsonInput, url), (long) (d * 1000), TimeUnit.MILLISECONDS));
		}

		for (Future<String> future : responses) {
			failedRequests = exceptionHandlingFuture(executor, failedRequests, future);
		}

		shutdownExecutorAndAwaitTermination(executor, 0);

		return failedRequests;
	}

	private void validateArgumentSize(List<String> args, int size) throws SeMoDeException {
		if (args.size() < size) {
			throw new SeMoDeException("Number of arguments invalid.");
		}
	}

	private String readJsonInput(String path) throws IOException, InvalidPathException {
		List<String> lines = Files.readAllLines(Paths.get(path));
		return lines.stream().collect(Collectors.joining(System.lineSeparator()));
	}

	private void shutdownExecutorAndAwaitTermination(ExecutorService executorService, int maxWaitTime) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(maxWaitTime, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
	}

	private int exceptionHandlingFuture(ExecutorService executorService, int failedRequests, Future<String> future)
			throws SeMoDeException {
		try {
			do{
				try {
					future.get();
				} catch (InterruptedException e) {
					logger.info("InterruptedException - investigate this orphan exception");
				}
			} while (!future.isDone());
		} catch (CancellationException | ExecutionException e) {
			logger.warn("ExecutionException", e);
			failedRequests++;
		}
		return failedRequests;
	}

}
