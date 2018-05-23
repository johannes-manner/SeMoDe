package de.uniba.dsg.serverless.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.uniba.dsg.serverless.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.benchmark.BenchmarkMode;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkUtility extends CustomUtility {

	private static final Logger logger = Logger.getLogger(BenchmarkUtility.class.getName());

	private String url;
	private String path;
	private String jsonInput;
	private BenchmarkMode mode;
	private int numberOfRequests;
	private int delay;
	int numberOfGroups;
	int numberOfRequestsEachGroup;

	public BenchmarkUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		try {
			initParameters(args);
		} catch (SeMoDeException e) {
			BenchmarkUtility.logger.log(Level.SEVERE, "Input Parameters not of currect format.", e);
			logUsage();
			return;
		}
		try {
			executeBenchmark();
		} catch (SeMoDeException e) {
			logger.log(Level.SEVERE, "Exception during benchmark execution.", e);
			return;
		}

	}

	private void logUsage() {
		BenchmarkUtility.logger.log(Level.SEVERE,
				"Usage for each mode:\n"
						+ "(Mode 1) URL JSONINPUT concurrent NUMBER_OF_REQUESTS\n"
						+ "(Mode 2) URL JSONINPUT sequentialInterval NUMBER_OF_REQUESTS DELAY\n"
						+ "(Mode 3) URL JSONINPUT sequentailWait NUMBER_OF_REQUESTS DELAY\n"
						+ "(Mode 4) URL JSONINPUT sequentialConcurrent NUMBER_OF_GROUPS NUMBER_OF_REQUESTS_GROUP DELAY");
	}

	private int executeBenchmark() throws SeMoDeException {
		BenchmarkExecutor executor = new BenchmarkExecutor(url, path, jsonInput);
		switch (mode) {
		case CONCURRENT:
			return executor.executeConcurrentBenchmark(numberOfRequests);
		case SEQUENTIAL_INTERVAL:
			executor.executeSequentialIntervalBenchmark(numberOfRequests, delay);
			return 1;
		case SEQUENTIAL_WAIT:
			executor.executeSequentialWaitBenchmark(numberOfRequests, delay);
			return 1;
		case SEQUENTIAL_CONCURRENT:
			return executor.executeSequentialConcurrentBenchmark(numberOfGroups, numberOfRequestsEachGroup, delay);
		default:
			throw new SeMoDeException("Mode is not implemented.");
		}
	}

	private void initParameters(List<String> args) throws SeMoDeException {
		validateArgumentSize(args, 4);
		try {
			url = Objects.requireNonNull(args.get(0));
			jsonInput = this.readJsonInput(args.get(1));
			mode = Objects.requireNonNull(BenchmarkMode.fromString(args.get(2)));

			switch (mode) {
			case CONCURRENT:
				validateArgumentSize(args, 4);
				numberOfRequests = Integer.parseInt(args.get(3));
				break;
			case SEQUENTIAL_INTERVAL:
				// Uses the same parameters as SEQUENTIAL_WAIT
			case SEQUENTIAL_WAIT:
				validateArgumentSize(args, 5);
				numberOfRequests = Integer.parseInt(args.get(3));
				delay = Integer.parseInt(args.get(4));
				break;
			case SEQUENTIAL_CONCURRENT:
				validateArgumentSize(args, 6);
				numberOfGroups = Integer.parseInt(args.get(3));
				numberOfRequestsEachGroup = Integer.parseInt(args.get(4));
				delay = Integer.parseInt(args.get(5));
				break;
			}

		} catch (NullPointerException e) {
			throw new SeMoDeException("A parameter is null. Check your command.", e);
		} catch (NumberFormatException e) {
			throw new SeMoDeException("Number of Requests must be a number.", e);
		} catch (IOException | InvalidPathException e) {
			throw new SeMoDeException("Error by reading the json from the file " + args.get(1), e);
		}
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

}
