package de.uniba.dsg.serverless.benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class LoadPatternGenerator {

	protected static final int PLATFORM_FUNCTION_TIMEOUT = 300;

	public LoadPatternGenerator() {

	}

	public Path generateConcurrentLoadPattern(List<String> args) throws SeMoDeException {
		List<Double> timestamps = new ArrayList<>();
		int numberOfRequests = Integer.parseInt(args.get(4));

		for (int i = 0; i < numberOfRequests; i++) {
			timestamps.add(0.0);
		}

		return this.writeLoadPatternToFile(timestamps, BenchmarkMode.CONCURRENT.getText());
	}

	public Path generateSequentialInterval(List<String> args) throws SeMoDeException {

		List<Double> timestamps = new ArrayList<>();

		int numberOfRequests = Integer.parseInt(args.get(4));
		int delay = Integer.parseInt(args.get(5));

		for (int i = 0; i < numberOfRequests; i++) {
			timestamps.add(0.0 + i * delay);
		}

		return this.writeLoadPatternToFile(timestamps, BenchmarkMode.SEQUENTIAL_INTERVAL.getText());
	}

	public Path generateSequentialConcurrent(List<String> args) throws SeMoDeException {

		List<Double> timestamps = new ArrayList<>();

		int numberOfGroups = Integer.parseInt(args.get(4));
		int numberOfRequestsEachGroup = Integer.parseInt(args.get(5));
		int delay = Integer.parseInt(args.get(6));

		for (int burst = 0; burst < numberOfGroups; burst++) {
			for (int i = 0; i < numberOfRequestsEachGroup; i++) {
				timestamps.add(0.0 + burst * delay);
			}
		}

		return this.writeLoadPatternToFile(timestamps, BenchmarkMode.SEQUENTIAL_CONCURRENT.getText());
	}

	public Path generateSequentialChangingInterval(List<String> args) throws SeMoDeException {

		List<Double> timestamps = new ArrayList<>();

		int numberOfRequests = Integer.parseInt(args.get(4));
		int used = 5;
		int[] delays = new int[args.size() - used];
		for (int i = 0; i < args.size() - used; i++) {
			delays[i] = Integer.parseInt(args.get(used + i));
		}

		double time = 0.0;
		for (int i = 0; i < numberOfRequests; i++) {
			timestamps.add(time);
			time += delays[i % delays.length];
		}

		return this.writeLoadPatternToFile(timestamps, BenchmarkMode.SEQUENTIAL_CHANGING_INTERVAL.getText());
	}

	private Path writeLoadPatternToFile(List<Double> timestamps, String fileName) throws SeMoDeException {
		try {
			List<String> lines = timestamps.stream().map(d -> "" + d).collect(Collectors.toList());
			return Files.write(Paths.get(fileName + ".csv"), lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new SeMoDeException("Was unable to write the load pattern csv.", e);
		}
	}

}
