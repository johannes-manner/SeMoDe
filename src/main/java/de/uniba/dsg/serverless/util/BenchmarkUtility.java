package de.uniba.dsg.serverless.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.benchmark.BenchmarkMode;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkUtility extends CustomUtility {

	private static final Logger logger = Logger.getLogger(BenchmarkUtility.class.getName());
	private URL url;
	private BenchmarkMode mode;
	int numberOfRequests;
	int delay;
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
			BenchmarkUtility.logger.log(Level.SEVERE,
					"Usage for each mode:\n"
							+ "(Mode 1) URL concurrent NUMBER_OF_REQUESTS\n"
							+ "(Mode 2) URL sequentialInterval NUMBER_OF_REQUESTS DELAY\n"
							+ "(Mode 3) URL sequentailWait NUMBER_OF_REQUESTS DELAY\n"
							+ "(Mode 4) URL sequentialConcurrent NUMBER_OF_GROUPS NUMBER_OF_REQUESTS_GROUP DELAY");
			return;
		}
		try {
			int numberOfFailedRequests = executeBenchmark(delay, mode);
			System.out.println(numberOfFailedRequests);
		} catch (SeMoDeException e) {
			logger.log(Level.SEVERE, "Exception during benchmark execution.", e);
			return;
		}

	}

	private int executeBenchmark(int delay, BenchmarkMode mode) throws SeMoDeException {
		BenchmarkExecutor executor = new BenchmarkExecutor(url);
		switch (mode) {
		case CONCURRENT:
			return executor.executeConcurrentBenchmark(numberOfRequests);
		case SEQUENTIAL_INTERVAL:
			return executor.executeSequentialIntervalBenchmark(numberOfRequests, delay);
		case SEQUENTIAL_WAIT:
			return executor.executeSequentialWaitBenchmark(numberOfRequests, delay);
		case SEQUENTIAL_CONCURRENT:
			return executor.executeSequentialConcurrentBenchmark(numberOfGroups, numberOfRequestsEachGroup, delay);
		default:
			throw new SeMoDeException("Mode is not implemented.");
		}
	}

	private void initParameters(List<String> args) throws SeMoDeException {
		validateArgumentSize(args, 2);
		try {
			url = new URL(args.get(0));
			mode = BenchmarkMode.fromString(args.get(1));

			switch (mode) {
			case CONCURRENT:
				validateArgumentSize(args, 3);
				numberOfRequests = Integer.parseInt(args.get(2));
				break;
			case SEQUENTIAL_INTERVAL:
				// Uses the same parameters as SEQUENTIAL_WAIT
			case SEQUENTIAL_WAIT:
				validateArgumentSize(args, 4);
				numberOfRequests = Integer.parseInt(args.get(2));
				delay = Integer.parseInt(args.get(3));
				break;
			case SEQUENTIAL_CONCURRENT:
				validateArgumentSize(args, 5);
				numberOfGroups = Integer.parseInt(args.get(2));
				numberOfRequestsEachGroup = Integer.parseInt(args.get(3));
				delay = Integer.parseInt(args.get(4));
				break;
			default:
				break;
			}

		} catch (MalformedURLException e) {
			throw new SeMoDeException("The URL could not be parsed.", e);
		} catch (NumberFormatException e) {
			throw new SeMoDeException("A number could not be parsed to an Integer.", e);
		}
	}

	private void validateArgumentSize(List<String> args, int size) throws SeMoDeException {
		if (args.size() < size) {
			throw new SeMoDeException("Number of arguments invalid.");
		}
	}

}
