package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BenchmarkParser {
    public static final String BENCHMARK_NAME = "benchmark.json";

    private final Path benchmarkPath;

    public BenchmarkParser(Path benchmarkPath) {
        this.benchmarkPath = benchmarkPath;
    }

    public double parseBenchmark() throws SeMoDeException {
        if (!Files.exists(benchmarkPath)) {
            throw new SeMoDeException("Benchmark file does not exist.");
        }
        try {
            List<String> lines = Files.readAllLines(benchmarkPath);
            String[] results = lines.get(lines.size() - 7).split("\\s+");
            return Double.parseDouble(results[3]);
        } catch (IOException e) {
            throw new SeMoDeException("Could not read file. ", e);
        }
    }

}
