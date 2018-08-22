package de.uniba.dsg.serverless.benchmark.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class EndpointExtractor {

	// Properties specific to serverless
	private static final String SERVICE_ESCAPE_SERVERLESS = "service: ";
	private static final String ENDPOINT_URL_SERVERLESS = "  GET - ";

	// Properties specific to maven
	private static final String SERVICE_ESCAPE_MAVEN = "[INFO] Successfully created Azure Functions ";
	private static final String ENDPOINT_URL_MAVEN = "[INFO] Successfully deployed Azure Functions at ";

	// Edit this based on the required log type!
	private final LogType logType;
	private final Path inputFilePath;
	private final Path benchmarkingRootPath;
	private final String language;
	private final String provider;
	// Change this to the function suffix (endpoint = url + suffix)
	private final String apiSuffix;

	public EndpointExtractor(String inputFilePath, Path benchmarkingRootPath, String language, String logType, String provider) {
		this.inputFilePath = Paths.get(inputFilePath);
		this.benchmarkingRootPath = benchmarkingRootPath;
		this.language = language;
		this.provider = provider;
		this.apiSuffix = "/api/fibonacci-" + language;
		this.logType = LogType.valueOf(logType);
		
	}

	public void extractEndpoints() throws SeMoDeException {
		
		Path outputFilePath = Paths.get(benchmarkingRootPath + "/" + BenchmarkingPipelineHandler.ENDPOINTS_FOLDER);
		BenchmarkingPipelineHandler.createFolderIfNotExists(outputFilePath);
		Path endpointFile = Paths.get(outputFilePath.toString() + "/" + this.provider + "-" + this.language);

		try (BufferedWriter writer = Files.newBufferedWriter(endpointFile, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
				String line = reader.readLine();
				String url;
				while (line != null) {
					switch (logType) {
					case MAVEN:
						if (line.startsWith(SERVICE_ESCAPE_MAVEN)) {
							writer.write(line.substring(SERVICE_ESCAPE_MAVEN.length()));
							writer.write(" ");
						} else if (line.startsWith(ENDPOINT_URL_MAVEN)) {
							url = line.substring(ENDPOINT_URL_MAVEN.length());
							writer.write(url + apiSuffix);
							writer.newLine();
						}
						break;
					case SERVERLESS:
						if (line.contains(SERVICE_ESCAPE_SERVERLESS)) {
							writer.write(line.substring(SERVICE_ESCAPE_SERVERLESS.length()));
							writer.write(" ");
						} else if (line.contains(ENDPOINT_URL_SERVERLESS)) {
							writer.write(line.substring(ENDPOINT_URL_SERVERLESS.length()));
							writer.newLine();
						}
						break;
					}
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Something went wrong during file operations. " + e.getMessage(), e);
		}
	}
}
