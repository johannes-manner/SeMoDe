package de.uniba.dsg.serverless.pipeline.benchmark.util;

import com.google.common.primitives.Doubles;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoadPatternGenerator {

    public static final int PLATFORM_FUNCTION_TIMEOUT = 300;
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
     * The first parameter is the number of execution groups, i.e. how many bulks are made.
     * The second parameter is the number of requests within each group.
     * The thrid parameters ist the delay between the termination of group g and the
     * start of group g+1 in seconds.
     *
     * @param parameters is a string, parameters mentioned above are separated via a blank.
     * @return
     * @throws SeMoDeException
     */
    public Path generateSequentialConcurrent(final String parameters) throws SeMoDeException {

        final List<Double> timestamps = new ArrayList<>();

        final int numberOfGroups = Integer.parseInt(parameters.split(" ")[0]);
        final int numberOfRequestsEachGroup = Integer.parseInt(parameters.split(" ")[1]);
        final int delay = Integer.parseInt(parameters.split(" ")[2]);

        for (int burst = 0; burst < numberOfGroups; burst++) {
            for (int i = 0; i < numberOfRequestsEachGroup; i++) {
                timestamps.add(0.0 + burst * delay);
            }
        }

        return this.writeLoadPatternToFile(timestamps);
    }

    /**
     * The first parameter is the number of total executions.
     * The second parameter is a list of delays between each individual request.
     * If the delays list is shorter than the number of total executions, the list is reused.
     *
     * @param parameters is a string, parameters mentioned above are separated via a blank.
     * @return
     * @throws SeMoDeException
     */
    public Path generateSequentialChangingInterval(final String parameters) throws SeMoDeException {

        final List<Double> timestamps = new ArrayList<>();

        final String[] parameterSplit = parameters.split(" ");
        final int numberOfRequests = Integer.parseInt(parameterSplit[0]);

        final int[] delays = new int[parameterSplit.length - 1];
        for (int i = 1; i < parameterSplit.length; i++) {
            delays[i - 1] = Integer.parseInt(parameterSplit[i]);
        }

        double time = 0.0;
        for (int i = 0; i < numberOfRequests; i++) {
            timestamps.add(time);
            time += delays[i % delays.length];
        }

        return this.writeLoadPatternToFile(timestamps);
    }

    /**
     * The first parameter is the file path to the .csv file.
     *
     * @param parameters is a string, parameters mentioned above are separated via a blank.
     */
    public void copyArbitraryLoadPattern(final String parameters) throws SeMoDeException {
        try {
            this.writeLoadPatternToFile(Files.readAllLines(Paths.get(parameters)).stream().map(Doubles::tryParse).collect(Collectors.toList()));
        } catch (final IOException e) {
            throw new SeMoDeException("Can't read arbitrary load pattern file: " + parameters, e);
        }
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
