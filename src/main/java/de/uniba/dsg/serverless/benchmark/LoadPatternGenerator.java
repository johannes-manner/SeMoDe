package de.uniba.dsg.serverless.benchmark;

import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoadPatternGenerator {

    protected static final int PLATFORM_FUNCTION_TIMEOUT = 300;
    private final Path loadPatternFile;

    public LoadPatternGenerator(final Path loadPatternFile) {
        this.loadPatternFile = loadPatternFile;
    }

    /**
     * Only a single parameter - check the documentation.
     * A bulk of timestamps and therefore requests are generated.
     *
     * @param parameters
     * @return
     * @throws SeMoDeException
     */
    public Path generateConcurrentLoadPattern(final String parameters) throws SeMoDeException {
        final List<Double> timestamps = new ArrayList<>();
        final int numberOfRequests = Integer.parseInt(parameters);

        for (int i = 0; i < numberOfRequests; i++) {
            timestamps.add(0.0);
        }

        return this.writeLoadPatternToFile(timestamps);
    }

    /**
     * The first parameter is <i>number of function executions</i>.
     * The second parameter is <i>time between the execution <b>start</b> times</i>!!
     *
     * @param parameters is a string, parameters mentioned above are separated via a blank.
     * @return
     * @throws SeMoDeException
     */
    public Path generateSequentialInterval(final String parameters) throws SeMoDeException {

        final List<Double> timestamps = new ArrayList<>();

        final int numberOfRequests = Integer.parseInt(parameters.split(" ")[0]);
        final int delay = Integer.parseInt(parameters.split(" ")[1]);

        for (int i = 0; i < numberOfRequests; i++) {
            timestamps.add(0.0 + i * delay);
        }

        return this.writeLoadPatternToFile(timestamps);
    }

    /**
     * @param args
     * @return
     * @throws SeMoDeException
     */
    public Path generateSequentialConcurrent(final List<String> args) throws SeMoDeException {

        final List<Double> timestamps = new ArrayList<>();

        final int numberOfGroups = Integer.parseInt(args.get(4));
        final int numberOfRequestsEachGroup = Integer.parseInt(args.get(5));
        final int delay = Integer.parseInt(args.get(6));

        for (int burst = 0; burst < numberOfGroups; burst++) {
            for (int i = 0; i < numberOfRequestsEachGroup; i++) {
                timestamps.add(0.0 + burst * delay);
            }
        }

        return this.writeLoadPatternToFile(timestamps, BenchmarkMode.SEQUENTIAL_CONCURRENT.getText());
    }

    public Path generateSequentialChangingInterval(final List<String> args) throws SeMoDeException {

        final List<Double> timestamps = new ArrayList<>();

        final int numberOfRequests = Integer.parseInt(args.get(4));
        final int used = 5;
        final int[] delays = new int[args.size() - used];
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

    private Path writeLoadPatternToFile(final List<Double> timestamps) throws SeMoDeException {
        try {
            final List<String> lines = timestamps.stream().map(d -> "" + d).collect(Collectors.toList());
            return Files.write(this.loadPatternFile, lines, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new SeMoDeException("Was unable to write the load pattern csv.", e);
        }
    }

}
