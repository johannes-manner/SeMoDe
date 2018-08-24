package de.uniba.dsg.serverless.pipeline.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkConfig;

public class BenchmarkingCommandGenerator {

	private final Path benchmarkCommandPath;
	private final Path endpointsPath;
	private final BenchmarkConfig benchmarkConfig;
	private static final String SEMODE_JAR_LOCATION = "../../../build/libs/SeMoDe.jar";

	public BenchmarkingCommandGenerator(Path benchmarkCommandPath, Path endpointsPath, BenchmarkConfig benchmarkConfig) {
		this.benchmarkCommandPath = benchmarkCommandPath;
		this.endpointsPath = endpointsPath;
		this.benchmarkConfig = benchmarkConfig;
	}

	public void generateCommands(String language, String provider) throws SeMoDeException {
		
		String providerLanguage = provider + "-" + language;

		Path outputCommands = Paths.get(this.benchmarkCommandPath.toString(), providerLanguage + ".bat");
		Path inputEndpoints = Paths.get(this.endpointsPath.toString(), providerLanguage);

		String firstPart = "start cmd /k java -jar " + SEMODE_JAR_LOCATION + " benchmark";
		// TODO params.json - how to deal with this file - locate in benchmarking commands folder
		String jsonInput = "params.json";

		try (BufferedWriter writer = Files.newBufferedWriter(outputCommands, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader reader = Files.newBufferedReader(inputEndpoints)) {
				String line = reader.readLine();
				while (line != null) {
					String[] functionPlusUrl = line.split(" ");
					String command = firstPart + " " + functionPlusUrl[0] + " " + functionPlusUrl[1] + " " + jsonInput
							+ " " + this.benchmarkConfig.getBenchmarkMode() + " " + this.benchmarkConfig.getBenchmarkParameters();
					writer.write(command);
					writer.newLine();
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Something went wrong during file operations. " + e.getMessage(), e);
		}
	}

}
