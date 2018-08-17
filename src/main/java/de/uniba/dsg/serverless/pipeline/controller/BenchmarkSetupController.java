package de.uniba.dsg.serverless.pipeline.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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

	public BenchmarkSetupController(BenchmarkSetup setup) {
		this.setup = setup;

	}

	public void initBenchmark() throws SeMoDeException {
		createBenchmarkFolder();
		gitIgnoreAll();
		initConfiguration();
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

	public void loadBenchmark() throws SeMoDeException {
		if (!Files.isDirectory(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		loadProperties();
		updateFields();
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
		for (DeploymentProperty property : setup.properties) {
			String rawProperty = setup.rawProperties.getProperty(property.key);
			if (rawProperty == null) {
				logger.warn("property not set");
				continue;
			}
			property.setRawValues(rawProperty);
		}
	}

	public void configureBenchmarkSetup() {
		String line;
		for (DeploymentProperty property : setup.properties) {
			printPropertyPrompt(property);
			line = PipelineSetupUtility.scanner.nextLine();
			while (!"".equals(line)) {
				try {
					property.setRawValues(line);
					setup.rawProperties.put(property.key, line);
					break;
				} catch (SeMoDeException e) {
					logger.warn("Format was not correct. (empty to skip property)");
					logger.warn(e.getMessage());
				}
				line = PipelineSetupUtility.scanner.nextLine();
			}
		}
	}

	private void printPropertyPrompt(DeploymentProperty property) {
		logger.info("Configuring property: \"" + property.key + "\"");
		String current = setup.rawProperties.getProperty(property.key);
		logger.info("Current assignment: " + ((current != null) ? current : "<not assigned yet>"));
		logger.info("Please specify the property. (empty to skip property)");
	}

}