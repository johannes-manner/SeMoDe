package de.uniba.dsg.serverless.simulation.load;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class LoadPatternInterpreter {

	private final Path file;

	public LoadPatternInterpreter(Path file) {
		super();
		this.file = file;
	}

	// TODO configure the interval for the load interpretation phase, currently
	// based on seconds
	public Map<Integer, Integer> interpretLoadPattern() throws SeMoDeException {

		List<String> lines = this.readAllLines(file);
		Map<Integer, Integer> numberOfRequestPerSecond = this.generateDistribution(lines);

		return numberOfRequestPerSecond;
	}

	public List<Double> getDoubleValues() throws SeMoDeException {
		List<String> lines = this.readAllLines(file);
		List<Double> values = new ArrayList<>();

		for (String line : lines) {
			try {
				values.add(Double.parseDouble(line));
			} catch (NumberFormatException e) {
				throw new SeMoDeException("Load pattern file was corrupted. Line: " + line, e);
			}
		}

		return values;
	}

	private Map<Integer, Integer> generateDistribution(List<String> lines) throws SeMoDeException {
		Map<Integer, Integer> distributionMap = new HashMap<>();
		for (Double doubleValue : this.getDoubleValues()) {

			// converts the double to the second before the comma
			int intValue = (int) doubleValue.doubleValue();
			if (!distributionMap.containsKey(intValue)) {
				distributionMap.put(intValue, 0);
			}
			distributionMap.put(intValue, distributionMap.get(intValue) + 1);
		}

		return distributionMap;
	}

	private List<String> readAllLines(Path file) throws SeMoDeException {
		try {
			List<String> lines = Files.readAllLines(file);
			return lines;
		} catch (IOException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}
	}
}
