package de.uniba.dsg.serverless.pipeline.benchmark.model;

import de.uniba.dsg.serverless.util.SeMoDeException;

public enum BenchmarkMode {
    CONCURRENT("concurrent"),
    SEQUENTIAL_INTERVAL("sequentialInterval"),
    SEQUENTIAL_CONCURRENT("sequentialConcurrent"),
    SEQUENTIAL_CHANGING_INTERVAL("sequentialChangingInterval"),
    ARBITRARY_LOAD_PATTERN("arbitraryLoadPattern");

    private final String text;

    BenchmarkMode(final String text) {
        this.text = text;
    }

    /**
     * Returns the {@link BenchmarkMode} associated to the given tag. Returns null
     * when no value exists for the given tag.
     *
     * @param tag
     * @return
     */
    public static BenchmarkMode fromString(final String tag) throws SeMoDeException {
        for (final BenchmarkMode mode : BenchmarkMode.values()) {
            if (mode.text.equalsIgnoreCase(tag)) {
                return mode;
            }
        }
        throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
    }

    public String getText() {
        return this.text;
    }
}
