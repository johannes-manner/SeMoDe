package de.uniba.dsg.serverless.simulation.load;

import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadPatternInterpreter {

    private final Path file;

    public LoadPatternInterpreter(final Path file) {
        super();
        this.file = file;
    }

    // TODO configure the interval for the load interpretation phase, currently
    // based on seconds
    public Map<Integer, Integer> interpretLoadPattern() throws SeMoDeException {

        final List<String> lines = this.readAllLines(this.file);
        final Map<Integer, Integer> numberOfRequestPerSecond = this.generateDistribution(lines);

        return numberOfRequestPerSecond;
    }

    public List<Double> getDoubleValues() throws SeMoDeException {
        final List<String> lines = this.readAllLines(this.file);
        final List<Double> values = new ArrayList<>();

        for (final String line : lines) {
            try {
                values.add(Double.parseDouble(line));
            } catch (final NumberFormatException e) {
                throw new SeMoDeException("Load pattern file was corrupted. Line: " + line, e);
            }
        }

        return values;
    }

    private Map<Integer, Integer> generateDistribution(final List<String> lines) throws SeMoDeException {
        final Map<Integer, Integer> distributionMap = new HashMap<>();
        for (final Double doubleValue : this.getDoubleValues()) {

            // converts the double to the second before the comma
            final int intValue = (int) doubleValue.doubleValue();
            if (!distributionMap.containsKey(intValue)) {
                distributionMap.put(intValue, 0);
            }
            distributionMap.put(intValue, distributionMap.get(intValue) + 1);
        }

        return distributionMap;
    }

    private List<String> readAllLines(final Path file) throws SeMoDeException {
        try {
            final List<String> lines = Files.readAllLines(file);
            return lines;
        } catch (final IOException e) {
            throw new SeMoDeException(e.getMessage(), e);
        }
    }
}
