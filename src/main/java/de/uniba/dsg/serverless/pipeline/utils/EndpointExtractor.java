package de.uniba.dsg.serverless.pipeline.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.LanguageConfig;

public class EndpointExtractor {

	// Properties specific to serverless
	private static final String SERVICE_ESCAPE_SERVERLESS = "service: ";
	private static final String ENDPOINT_URL_SERVERLESS = "  GET - ";

	// Properties specific to maven
	private static final String SERVICE_ESCAPE_MAVEN = "[INFO] Successfully created Azure Functions ";
	private static final String ENDPOINT_URL_MAVEN = "[INFO] Successfully deployed Azure Functions at ";

	private final Map<String, LanguageConfig> languageConfigs;
	private final Path deploymentFolderPath;
	private final Path outputDir;

	public EndpointExtractor(Map<String, LanguageConfig> languageConfigs, Path deploymentFolderPath, Path outputDir) {
		this.languageConfigs = languageConfigs;
		this.deploymentFolderPath = deploymentFolderPath;
		this.outputDir = outputDir;
	}

	public void extractEndpoints(String language, String provider) throws SeMoDeException {
		
		String providerLanguage = provider + "-" + language;
		String apiSuffix = "/api/fibonacci-" + language;
		
		Path endpointFile = Paths.get(outputDir.toString(), providerLanguage);
		Path inputFilePath = Paths.get(this.deploymentFolderPath.toString(), providerLanguage + "-deploy");

		try (BufferedWriter writer = Files.newBufferedWriter(endpointFile, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
				String line = reader.readLine();
				String url;
				String logType = languageConfigs.get(providerLanguage).getLogType();
				while (line != null) {
					switch (logType) {
					case "maven":
						if (line.startsWith(SERVICE_ESCAPE_MAVEN)) {
							writer.write(line.substring(SERVICE_ESCAPE_MAVEN.length()));
							writer.write(" ");
						} else if (line.startsWith(ENDPOINT_URL_MAVEN)) {
							url = line.substring(ENDPOINT_URL_MAVEN.length());
							writer.write(url + apiSuffix);
							writer.newLine();
						}
						break;
					case "serverless":
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
