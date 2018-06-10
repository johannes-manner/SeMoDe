package de.uniba.dsg.serverless.cli;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.benchmark.BenchmarkMode;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(BenchmarkUtility.class.getName());

	private URL url;
	private String jsonInput;
	private BenchmarkMode mode;
	private int numberOfRequests;
	private int delay;
	private int numberOfGroups;
	private int numberOfRequestsEachGroup;
	private int[] delays;

	public BenchmarkUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		try {
			initParameters(args);
		} catch (SeMoDeException e) {
			logger.fatal("Input Parameters not of currect format.", e);
			logUsage();
			return;
		}
		try {
			executeBenchmark();
		} catch (SeMoDeException e) {
			logger.fatal("Exception during benchmark execution.", e);
			return;
		}

	}

	private void logUsage() {
		logger.fatal("Usage for each mode:\n" + "(Mode 1) URL JSONINPUT concurrent NUMBER_OF_REQUESTS\n"
				+ "(Mode 2) URL JSONINPUT sequentialInterval NUMBER_OF_REQUESTS DELAY\n"
				+ "(Mode 3) URL JSONINPUT sequentailWait NUMBER_OF_REQUESTS DELAY\n"
				+ "(Mode 4) URL JSONINPUT sequentialConcurrent NUMBER_OF_GROUPS NUMBER_OF_REQUESTS_GROUP DELAY\n"
				+ "(Mode 4) URL JSONINPUT sequentialChangingInterval NUMBER_OF_REQUESTS (DELAY)+");
	}

	private int executeBenchmark() throws SeMoDeException {
		BenchmarkExecutor executor = new BenchmarkExecutor(url, jsonInput);

		switch (mode) {
		case CONCURRENT:
			return executor.executeConcurrentBenchmark(numberOfRequests);
		case SEQUENTIAL_INTERVAL:
			return executor.executeSequentialIntervalBenchmark(numberOfRequests, delay);
		case SEQUENTIAL_WAIT:
			return executor.executeSequentialWaitBenchmark(numberOfRequests, delay);
		case SEQUENTIAL_CONCURRENT:
			return executor.executeSequentialConcurrentBenchmark(numberOfGroups, numberOfRequestsEachGroup, delay);
		case SEQUENTIAL_CHANGING_INTERVAL:
			return executor.executeSequentialChangingIntervalBenchmark(numberOfRequests, delays);
		case SEQUENTIAL_CHANGING_WAIT:
			return executor.executeSequentialChangingWaitBenchmark(numberOfRequests, delays);
		default:
			throw new SeMoDeException("Mode " + mode + " is not implemented.");
		}
	}

	private void initParameters(List<String> args) throws SeMoDeException {
		// minimum amount of arguments
		validateArgumentSize(args, 4);
		try {
			url = new URL(args.get(0));
			jsonInput = this.readJsonInput(args.get(1));
			mode = BenchmarkMode.fromString(args.get(2));

			switch (mode) {
			case CONCURRENT:
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
			case SEQUENTIAL_CHANGING_INTERVAL:
				// Uses the same parameters as SEQUENTIAL_CHANGING_WAIT
			case SEQUENTIAL_CHANGING_WAIT:
				validateArgumentSize(args, 5);
				numberOfRequests = Integer.parseInt(args.get(3));
				int used = 4;
				delays = new int[args.size() - used];
				for (int i = 0; i < args.size() - used; i++) {
					delays[i] = Integer.parseInt(args.get(used + i));
				}
				break;
			default:
				throw new SeMoDeException("Mode " + mode + " is not implemented.");
			}
		} catch (NumberFormatException e) {
			throw new SeMoDeException(e.getMessage(), e);
		} catch (MalformedURLException e) {
			throw new SeMoDeException("Malformed URL " + args.get(0), e);
		} catch (IOException | InvalidPathException e) {
			throw new SeMoDeException("Exception while reading the json from the file " + args.get(1), e);
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
