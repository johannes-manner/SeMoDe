package de.uniba.dsg.serverless.benchmark;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum BenchmarkMode {
	CONCURRENT("concurrent"),
	SEQUENTIAL_INTERVAL("sequentialInterval"),
	SEQUENTIAL_WAIT("sequentailWait"),
	SEQUENTIAL_CONCURRENT("sequentialConcurrent");

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
		for (BenchmarkMode b : BenchmarkMode.values()) {
			if (b.text.equalsIgnoreCase(tag)) {
				return b;
			}
		}
		throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
	}
}
