package de.uniba.dsg.serverless.benchmark;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum BenchmarkMode {
	CONCURRENT("concurrent"),
	SEQUENTIAL_INTERVAL("sequentialInterval"),
	SEQUENTIAL_CONCURRENT("sequentialConcurrent"),
	SEQUENTIAL_CHANGING_INTERVAL("sequentialChangingInterval"),
	ARBITRARY_LOAD_PATTERN("arbitraryLoadPattern");

	private String text;

	BenchmarkMode(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	/**
	 * Returns the {@link BenchmarkMode} associated to the given tag. Returns null
	 * when no value exists for the given tag.
	 * 
	 * @param tag
	 * @return
	 */
	public static BenchmarkMode fromString(String tag) throws SeMoDeException {
		for (BenchmarkMode mode : BenchmarkMode.values()) {
			if (mode.text.equalsIgnoreCase(tag)) {
				return mode;
			}
		}
		throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
	}
}
