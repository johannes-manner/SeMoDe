package de.uniba.dsg.serverless.pipeline.benchmark.model;

import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.Data;

import java.util.List;

@Data
public class BenchmarkMode {
    public static final String CONCURRENT = "concurrent";
    public static final String SEQUENTIAL_INTERVAL = "sequentialInterval";
    public static final String SEQUENTIAL_CONCURRENT = "sequentialConcurrent";
    public static final String SEQUENTIAL_CHANGING_INTERVAL = "sequentialChangingInterval";
    public static final String ARBITRARY_LOAD_PATTERN = "arbitraryLoadPattern";

    public static List<BenchmarkMode> availableModes = List.of(
            new BenchmarkMode(CONCURRENT,
                    "Executing once N requests in parallel",
                    List.of("Number of requests")),
            new BenchmarkMode(SEQUENTIAL_INTERVAL,
                    "Sequential triggering of a funtion with a fixed time interval between triggering it",
                    List.of("Number of requests",
                            "Time between the request execution start times")),
            new BenchmarkMode(SEQUENTIAL_CONCURRENT,
                    "Sequential combined with concurrent triggering of a function. Multiple sequential groups of requests execute functions concurrently.",
                    List.of("Number of execution groups",
                            "Number of requests in each group",
                            "Delay between termination of group g and start of group g+1 in seconds")),
            new BenchmarkMode(SEQUENTIAL_CHANGING_INTERVAL,
                    "This mode triggers functions in an interval with varying delays between execution start times.",
                    List.of("Number of requests",
                            "List of delays")),
            new BenchmarkMode(ARBITRARY_LOAD_PATTERN,
                    "This mode triggers the function endpoint based on a csv file. The file contains double values, when a specific call should be submitted.",
                    List.of("File name of the csv load pattern file.")));

    private String text;
    private String information;
    private List<String> parameters;

    BenchmarkMode(final String text, final String information, final List<String> parameters) {
        this.text = text;
        this.information = information;
        this.parameters = parameters;
    }

    /**
     * Returns the {@link BenchmarkMode} associated to the given tag. Returns null when no value exists for the given
     * tag.
     */
    public static BenchmarkMode fromString(final String tag) throws SeMoDeException {
        for (final BenchmarkMode mode : BenchmarkMode.availableModes) {
            if (mode.getText().equalsIgnoreCase(tag)) {
                return mode;
            }
        }
        throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
    }

    public String getText() {
        return this.text;
    }
}
