package de.uniba.dsg.serverless.pipeline.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;
import de.uniba.dsg.serverless.pipeline.model.ProviderConfig;

public class BenchmarkSetupController {

	private static final Logger logger = LogManager.getLogger(BenchmarkSetupController.class.getName());
	private final ObjectMapper om;

	private final BenchmarkSetup setup;

	private BenchmarkSetupController(BenchmarkSetup setup) {
		this.setup = setup;
		om = new ObjectMapper();
	}

	public static BenchmarkSetupController init(BenchmarkSetup setup) throws SeMoDeException {
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		controller.createBenchmarkFolder();
		return controller;
	}

	public static BenchmarkSetupController load(BenchmarkSetup setup) throws SeMoDeException {
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		if (!Files.isDirectory(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		setup.loadProviders(setup.pathToConfig.toString());
		return controller;
	}

	private void createBenchmarkFolder() throws SeMoDeException {
		if (Files.exists(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup already exists. Choose a different name.");
		}
		try {
			Files.createDirectories(setup.pathToSetup);
		} catch (IOException e) {
			throw new SeMoDeException(e);
		}
	}

	public void configureBenchmarkSetup() {
		String provider;
		do {
			System.out.println("Insert a valid provider: " + setup.possibleProviders.keySet().toString());
			provider = PipelineSetupUtility.scanner.nextLine();
			if (setup.possibleProviders.containsKey(provider)) {
				List<String> providerJson = readProviderProperties(provider);
				String json = "{" + providerJson.stream().collect(Collectors.joining(",")) + "}";
				System.out.println(json);
				try {
					ProviderConfig p = om.readValue(json, ProviderConfig.class);
					setup.properties.put(p.getName(), p);
				} catch (IOException e) {
					System.out.println("Incorrect json format: " + json);
				}
			}
		} while (!"".equals(provider));

	}

	private List<String> readProviderProperties(String provider) {

		List<String> providerProperties = new ArrayList<>();
		for (String key : ProviderConfig.jsonProviderProperties()) {
			printPropertyPrompt(key);
			String line = PipelineSetupUtility.scanner.nextLine();
			providerProperties.add("\"" + key + "\": " + line);
			logger.info("Successfully stored property " + key);
		}
		return providerProperties;
	}

	private void printPropertyPrompt(String key) {
		System.out.println("Configure property \"" + key + "\"");
		System.out.println(
				"Please specify the property. Think about the correct JSON representation for the value. \n(empty to skip property)");
	}

	public void saveBenchmarkSetup() throws SeMoDeException {

		try {
			om.writer().withDefaultPrettyPrinter().writeValue(Paths.get(setup.pathToConfig.toString()).toFile(),
					setup.properties.values());
		} catch (IOException e) {
			throw new SeMoDeException("Configuration could not be saved.", e);
		}

	}

	public void printBenchmarkSetupStatus() throws SeMoDeException {
		System.out.println("Printing status of benchmark setup \"" + setup.name + "\"");
		System.out.println("Printing Properties:");
		for (String key : setup.properties.keySet()) {
			System.out.println(setup.properties.get(key));
		}
	}

	public void prepareDeployment() throws SeMoDeException {
		System.out.println("copying sources...");
		for (String provider : setup.properties.keySet()) {
			for (String language : setup.properties.get(provider).getLanguage()) {
				copySource(provider, language);
			}
		}
		System.out.println("creating deployment sizes");
		// TODO run bash scripts
		ProcessBuilder processBuilder = new ProcessBuilder("CMD", "/C", "echo", "RUN BASH SCRIPTS HERE");
		processBuilder.directory(new File(setup.pathToSetup.toString())); // selectcorrect path here
		try {
			Process process = processBuilder.start();
			int errCode = process.waitFor();
			System.out.println("Executed without errors? " + (errCode == 0 ? "Yes" : "No(code=" + errCode + ")"));
			System.out.println("Output:\n" + output(process.getInputStream()));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String output(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));) {
			String line = bufferedReader.readLine();
			while (line != null) {
				sb.append(line + System.getProperty("line.separator"));
				line = bufferedReader.readLine();
			}
		}
		return sb.toString();
	}

	private void copySource(String provider, String language) throws SeMoDeException {
		String sourceFolderName = provider + "-" + language;
		File source = new File(Paths.get("fibonacci", sourceFolderName).toString());
		File target = new File(setup.pathToFibonacciSources.resolve(sourceFolderName).toString());
		try {
			FileUtils.copyDirectory(source, target);
		} catch (IOException e) {
			throw new SeMoDeException("Copying the source of " + sourceFolderName + "failed.", e);
		}
	}
	
	// TODO : validate providers, when reading from cli
}
