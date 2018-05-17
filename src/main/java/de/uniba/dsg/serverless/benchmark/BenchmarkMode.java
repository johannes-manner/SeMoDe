package de.uniba.dsg.serverless.benchmark;

public enum BenchmarkMode {
	CONCURRENT("concurrent"), SEQUENTIAL_INTERVAL("sequentialInterval"), SEQUENTIAL_WAIT("sequentailWait");

	private String text;

	BenchmarkMode(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static BenchmarkMode fromString(String text) {
		for (BenchmarkMode b : BenchmarkMode.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
