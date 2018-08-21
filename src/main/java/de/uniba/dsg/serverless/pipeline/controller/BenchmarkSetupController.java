package de.uniba.dsg.serverless.pipeline.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;
import de.uniba.dsg.serverless.pipeline.model.DeploymentProperty;

public class BenchmarkSetupController {

	private static final Logger logger = LogManager.getLogger(BenchmarkSetupController.class.getName());

	private final BenchmarkSetup setup;
	private final String CONFIG_COMMENT = "Configuration file of the benchmarking setups.\n"
			+ "The Format defined in de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetup";

	private BenchmarkSetupController(BenchmarkSetup setup) {
		this.setup = setup;
	}

	public static BenchmarkSetupController init(BenchmarkSetup setup) throws SeMoDeException {
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		controller.createBenchmarkFolder();
		controller.gitIgnoreAll();
		controller.initConfiguration();
		return controller;
	}

	public static BenchmarkSetupController load(BenchmarkSetup setup) throws SeMoDeException {
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		if (!Files.isDirectory(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		controller.loadProperties();
		controller.updateFields();
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

	private void gitIgnoreAll() throws SeMoDeException {
		Path pathToGitIgnore = Paths.get(BenchmarkSetup.SETUP_LOCATION, ".gitignore");
		if (!Files.exists(pathToGitIgnore)) {
			try {
				Files.write(pathToGitIgnore, "*".getBytes());
			} catch (IOException e) {
				throw new SeMoDeException(e);
			}
		}
	}

	private void initConfiguration() throws SeMoDeException {
		setup.rawProperties = new Properties();
		setup.rawProperties.put("name", setup.name);
		try (FileWriter writer = new FileWriter(setup.pathToConfig.toString())) {
			setup.rawProperties.store(writer, CONFIG_COMMENT);
		} catch (IOException e) {
			throw new SeMoDeException("Configuration could not be initialized.", e);
		}
	}

	private void loadProperties() throws SeMoDeException {
		try (Reader reader = new FileReader(new File(setup.pathToConfig.toString()))) {
			setup.rawProperties = new Properties();
			setup.rawProperties.load(reader);
		} catch (IOException e) {
			throw new SeMoDeException("Configuration could not be loaded.", e);
		}
	}

	private void updateFields() throws SeMoDeException {
		for (String key : setup.properties.keySet()) {
			String rawProperty = setup.rawProperties.getProperty(key);
			if (rawProperty == null) {
				logger.warn("Property " + key + " is not yet assigned.");
				continue;
			}
			setup.getProperty(key).setRawValues(rawProperty);
		}
	}

	public void configureBenchmarkSetup() {
		for (String key : setup.properties.keySet()) {
			printPropertyPrompt(key);
			String line = PipelineSetupUtility.scanner.nextLine();
			while (!"".equals(line)) {
				try {
					setup.getProperty(key).setRawValues(line);
					setup.rawProperties.put(key, line);
					logger.info("Successfully stored property " + key);
					break;
				} catch (SeMoDeException e) {
					System.out.println("Format was not correct. (empty to skip property)");
					System.out.println("\t" + e.getMessage());
					System.out.println("\t" + "Please try again.");
				}
				printPropertyPrompt(key);
				line = PipelineSetupUtility.scanner.nextLine();
			}
		}
	}

	private void printPropertyPrompt(String key) {
		System.out.println("Configure property \"" + key + "\"");
		String current = setup.rawProperties.getProperty(key);
		System.out.println("Current assignment: " + ((current != null) ? current : "<not assigned yet>"));
		System.out.println("Please specify the property. (empty to skip property)");
	}

	public void saveBenchmarkSetup() throws SeMoDeException {
		try (FileWriter writer = new FileWriter(setup.pathToConfig.toString())) {
			setup.rawProperties.store(writer, CONFIG_COMMENT);
			logger.info("Successfully saved configuration to file " + setup.pathToConfig.toString());
		} catch (IOException e) {
			throw new SeMoDeException("Configuration could not be saved.", e);
		}
	}

	public void printBenchmarkSetupStatus() throws SeMoDeException {
		System.out.println("Printing status of benchmark setup \"" + setup.name + "\"");
		System.out.println("Printing Properties:");
		for (String key : setup.properties.keySet()) {
			DeploymentProperty property = setup.getProperty(key);
			String propertyInfo = "<not yet assigned>";
			if (property.getValues() != null) {
				propertyInfo = property.getValues().stream().reduce((a, b) -> a + ", " + b).get();
			}
			System.out.println("Property " + key + ": " + propertyInfo);
		}
	}

	public void prepareDeployment() throws SeMoDeException {
		DeploymentProperty providerProperty = setup.getProperty("provider");
		DeploymentProperty languageProperty = setup.getProperty("language");
		providerProperty.requireValuesNonNull();
		languageProperty.requireValuesNonNull();
		System.out.println("copying sources...");
		for (String provider : providerProperty.getValues()) {
			for (String language : languageProperty.getValues()) {
				// copySource(provider, language);
			}
		}
		logger.info("Successfully copied sources of selected languages / providers.");
		System.out.println("creating deployment sizes");
		// TODO run bash scripts
		ProcessBuilder processBuilder = new ProcessBuilder("CMD", "/C", "echo", "RUN BASH SCRIPTS HERE");
		processBuilder.directory(new File(setup.pathToSetup.toString())); // select correct path here
		try {
			Process process = processBuilder.start();
			int errCode = process.waitFor();
			System.out.println("Executed without errors? " + (errCode == 0 ? "Yes" : "No (code=" + errCode + ")"));
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

}
