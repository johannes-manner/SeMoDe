package de.uniba.dsg.serverless.pipeline.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	// --------

	public void loadBenchmark() throws SeMoDeException {
		if (!Files.isDirectory(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		loadProperties();
		updateFields();
	}

	private void loadProperties() throws SeMoDeException {
		try (Reader reader = new FileReader(new File(""))) {
			setup.rawProperties = new Properties();
			setup.rawProperties.load(reader);
		} catch (IOException e) {
			throw new SeMoDeException("Configuration could not be loaded.", e);
		}
	}

	private void updateFields() throws SeMoDeException {
		// TODO improve this solution to work more generically
		// possible solution: add additional parameter to deployment Property which
		// specifies the type of the information stored

		for (DeploymentProperty property : setup.properties) {
			String rawProperty = setup.rawProperties.getProperty(property.key);
			if (rawProperty == null) {
				continue;
			}
			List<String> propertyString = Arrays.asList(rawProperty.split(BenchmarkSetup.SEPERATOR));
			if (!propertyString.isEmpty() && propertyString.get(0).matches("[0-9]+")) {
				List<Integer> propertyInteger = propertyString.stream().map(Integer::parseInt)
						.collect(Collectors.toList());
				property.setValues(propertyInteger);
			} else {
				property.setValues(propertyString);
			}
		}
	}

	public void configureBenchmarkSetup() {
		for (DeploymentProperty property : setup.properties) {
			// TODO decide whether or not to use logger here
			System.out.println("Configuring the property " + property.key);
			String current = setup.rawProperties.getProperty(property.key);
			current = (current != null) ? current : "<not assigned yet>";
			System.out.println("Current assignment: " + current);
			System.out.println("Please specify the property. (empty to skip)");
			
		}
	}

}
