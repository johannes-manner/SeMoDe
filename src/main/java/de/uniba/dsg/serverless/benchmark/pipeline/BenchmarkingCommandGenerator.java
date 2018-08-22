package de.uniba.dsg.serverless.benchmark.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkingCommandGenerator {

	private final Path benchmarkingRootPath;
	private final String language;
	private final String provider;

	public BenchmarkingCommandGenerator(Path benchmarkingRootPath, String language, String provider) {
		this.benchmarkingRootPath = benchmarkingRootPath;
		this.language = language;
		this.provider = provider;
	}

	public void generateCommands(String benchmarkingMode, String benchmarkingParameters) throws SeMoDeException {

		Path commandFolder = Paths
				.get(benchmarkingRootPath + "/" + BenchmarkingPipelineHandler.BENCHMARKING_COMMANDS_FOLDER);
		BenchmarkingPipelineHandler.createFolderIfNotExists(commandFolder);

		Path outputCommands = Paths.get(commandFolder + "/" + this.provider + "-" + this.language + ".bat");
		Path inputEndpoints = Paths.get(this.benchmarkingRootPath + "/" + BenchmarkingPipelineHandler.ENDPOINTS_FOLDER
				+ "/" + this.provider + "-" + this.language);

		String firstPart = "start cmd /k java -jar build/libs/SeMoDe.jar benchmark";
		String jsonInput = "params.json";

		try (BufferedWriter writer = Files.newBufferedWriter(outputCommands, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader reader = Files.newBufferedReader(inputEndpoints)) {
				String line = reader.readLine();
				while (line != null) {
					String[] functionPlusUrl = line.split(" ");
					String command = firstPart + " " + functionPlusUrl[0] + " " + functionPlusUrl[1] + " " + jsonInput
							+ " " + benchmarkingMode + " " + benchmarkingParameters;
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
