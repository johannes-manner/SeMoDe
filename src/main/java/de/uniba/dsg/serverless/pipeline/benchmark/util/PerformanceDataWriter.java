package de.uniba.dsg.serverless.pipeline.benchmark.util;

import de.uniba.dsg.serverless.pipeline.benchmark.log.LogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.model.WritableEvent;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Writes the performance data locally and the platform data from the
 * corresponding provider to files.
 */
final public class PerformanceDataWriter {

    public static final double NETWORK_AND_PLATFORM_DELAY = 5000.0;

    public void writePerformanceDataToFile(final Path file, final LogHandler logHandler, final BenchmarkingRESTAnalyzer restHandler) throws SeMoDeException {
        this.writePerformanceDataToFile(file, restHandler.extractRESTEvents(), logHandler.getPerformanceData());
    }

    private void writePerformanceDataToFile(final Path file, final Map<String, WritableEvent> restMap,
                                            final Map<String, WritableEvent> performanceProviderMap) throws SeMoDeException {
        try {
            try (final BufferedWriter writer = Files.newBufferedWriter(file)) {

                // If no platform data is available - exit the program
                if (performanceProviderMap.isEmpty()) {
                    throw new SeMoDeException(
                            "The platform map is empty. The most likely reason is the wrong start and end time.");
                }

                if (restMap.isEmpty()) {
                    this.writeOnlyPerformanceDataToFile(writer, performanceProviderMap);
                } else {
                    this.writeRESTAndPerformanceDataToFile(writer, restMap, performanceProviderMap);
                }

            }
        } catch (final IOException e) {
            throw new SeMoDeException("Writing to file failed");
        }
    }

    private void writeRESTAndPerformanceDataToFile(final BufferedWriter writer, final Map<String, WritableEvent> restMap,
                                                   final Map<String, WritableEvent> performanceProviderMap) throws IOException {

        // write header lines
        this.writeHeaderLines(writer, restMap);
        this.writeHeaderLines(writer, performanceProviderMap);

        writer.write(System.lineSeparator());

        // Writing rows
        for (final String key : restMap.keySet()) {
            final WritableEvent restEvent = restMap.get(key);
            writer.write(restEvent.toCSVString());

            final WritableEvent providerEvent = performanceProviderMap.get(key);

            if (providerEvent != null) {
                writer.write(providerEvent.toCSVString());
            }
            writer.write(System.lineSeparator());
        }
    }

    private void writeOnlyPerformanceDataToFile(final BufferedWriter writer,
                                                final Map<String, WritableEvent> performanceProviderMap) throws IOException {

        // Writing header lines
        this.writeHeaderLines(writer, performanceProviderMap);
        writer.write(System.lineSeparator());

        // Writing rows
        for (final String key : performanceProviderMap.keySet()) {
            writer.write(performanceProviderMap.get(key).toCSVString());
            writer.write(System.lineSeparator());
        }
    }

    private void writeHeaderLines(final BufferedWriter writer, final Map<String, WritableEvent> map) throws IOException {
        final String key = map.keySet().iterator().next();
        writer.write(map.get(key).getCSVMetadata());
    }
}