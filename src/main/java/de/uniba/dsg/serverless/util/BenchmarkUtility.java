package de.uniba.dsg.serverless.util;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

	public BenchmarkUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {

		try {
			initParameters(args);
		} catch (SeMoDeException e) {
			logger.log(Level.SEVERE, "Input Parameters not of currect format.", e);
			BenchmarkUtility.logger.log(Level.SEVERE, "Usage: " + "\n(1) URL" + "\n(2) Mode"
					+ "\n(3) Number of Requests" + "\n(4) Time Between Requests (only for sequential)");
			return;
		}
		try {
			BenchmarkExecutor executor = new BenchmarkExecutor(url, path, jsonInput, numberOfRequests);
			executor.executeBenchmark(delay, mode);
		} catch (SeMoDeException | MalformedURLException e) {
			logger.log(Level.SEVERE, "Exception during benchmark execution.", e);
			return;
		}

	}

	private void initParameters(List<String> args) throws SeMoDeException {
		if (args.size() < 4) {
			throw new SeMoDeException("Wrong parameter size.");
		}
		try {
			url = Objects.requireNonNull(args.get(0));
			jsonInput = this.readJsonInput(args.get(1));
			mode = Objects.requireNonNull(BenchmarkMode.fromString(args.get(2)));
			numberOfRequests = Integer.parseInt(args.get(3));
		}  catch (NullPointerException e) {
			throw new SeMoDeException("A parameter is missing. Check your command.", e);
		} catch (NumberFormatException e) {
			throw new SeMoDeException("Number of Requests must be a number.", e);
		}

		if (mode == BenchmarkMode.SEQUENTIAL_INTERVAL || mode == BenchmarkMode.SEQUENTIAL_WAIT) {
			if (args.size() < 5) {
				throw new SeMoDeException("Time Parameter missing for Mode Sequential");
			}
			if (!isNumeric(args.get(4))) {
				throw new SeMoDeException("Number of Requests must be a number.");
			}
			delay = Integer.parseInt(args.get(4));
		}
	}

	private String readJsonInput(String string) {
		// TODO:
		return "{\"number\": 30}";
	}

	private static boolean isNumeric(String s) {
		return Pattern.matches("\\d+", s);
	}

}
