package de.uniba.dsg.serverless.pipeline.utils;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BenchmarkingCommandGenerator {

    private final Path benchmarkCommandPath;
    private final Path endpointsPath;
    private final BenchmarkConfig benchmarkConfig;
    private final String seMoDeJarLocation;

    public BenchmarkingCommandGenerator(final Path benchmarkCommandPath, final Path endpointsPath, final BenchmarkConfig benchmarkConfig, final String seMoDeJarLocation) {
        this.benchmarkCommandPath = benchmarkCommandPath;
        this.endpointsPath = endpointsPath;
        this.benchmarkConfig = benchmarkConfig;
        this.seMoDeJarLocation = seMoDeJarLocation;
    }

    public void generateCommands(final String language, final String provider) throws SeMoDeException {

        final String providerLanguage = provider + "-" + language;

        final Path outputCommands = Paths.get(this.benchmarkCommandPath.toString(), providerLanguage + ".bat");
        final Path inputEndpoints = Paths.get(this.endpointsPath.toString(), providerLanguage);

        // TODO test xterm under Windows (Git Bash)
        final String firstPart = "xterm -e 'sh -c \"java -jar " + this.seMoDeJarLocation + " benchmark";
        // TODO params.json - how to deal with this file - locate in benchmarking commands folder
        final String jsonInput = "params.json";

        try (final BufferedWriter writer = Files.newBufferedWriter(outputCommands, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            try (final BufferedReader reader = Files.newBufferedReader(inputEndpoints)) {
                String line = reader.readLine();
                while (line != null) {
                    final String[] functionPlusUrl = line.split(" ");
                    final String command = firstPart + " " + provider + "_" + functionPlusUrl[0] + " " + functionPlusUrl[1] + " " + jsonInput
                            + " " + this.benchmarkConfig.getBenchmarkMode() + " " + this.benchmarkConfig.getNumberThreads() + " " + this.benchmarkConfig.getBenchmarkParameters()
                            + "\"'";
                    writer.write(command);
                    writer.newLine();
                    line = reader.readLine();
                }
            }
        } catch (final IOException e) {
            throw new SeMoDeException("Something went wrong during file operations. " + e.getMessage(), e);
        }
    }

}
