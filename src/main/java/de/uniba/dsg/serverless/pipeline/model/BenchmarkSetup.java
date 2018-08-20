package de.uniba.dsg.serverless.pipeline.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import de.uniba.dsg.serverless.model.SeMoDeException;

/**
 * Model class of a Benchmark Setup. A benchmark setup consists of multiple
 * benchmarks. The settings are defined in properties file manually or
 * automatically. The format of the property file is as follows:
 * <p>
 * key=setting1,setting2,settingN
 * <p>
 * with keys being listed below.<br>
 * Resulting benchmarking setup contains all possible combinations of the
 * settings.
 * 
 */
public class BenchmarkSetup {

	public static final String SETUP_LOCATION = "setups";
	public static final String SEPERATOR = ";";

	public final List<DeploymentProperty> properties;
	public Properties rawProperties;

	public final String name;
	public final Path pathToSetup;
	public final Path pathToConfig;
	public final Path pathToFibonacciSources;

	public BenchmarkSetup(String name) throws SeMoDeException {
		this.name = name;
		this.pathToSetup = Paths.get(BenchmarkSetup.SETUP_LOCATION, name);
		this.pathToConfig = pathToSetup.resolve("settings.config");
		this.pathToFibonacciSources = pathToSetup.resolve("sources");
		properties = new ArrayList<>();
		initializeProperties();
	}

	/**
	 * Note that these properties might change and must be extended in the future.
	 * 
	 * @throws SeMoDeException
	 */
	private void initializeProperties() throws SeMoDeException {
		properties.add(new DeploymentProperty("language", String.class, Arrays.asList("java", "js")));
		properties.add(new DeploymentProperty("provider", String.class, Arrays.asList("aws", "azure")));
		properties.add(new DeploymentProperty("deploymentSize", Integer.class));
		// Limits: https://docs.aws.amazon.com/de_de/lambda/latest/dg/limits.html
		List<String> possibleMemorySettings = new ArrayList<>();
		for (int setting = 128; setting <= 3008; setting += 64) {
			possibleMemorySettings.add(String.valueOf(setting));
		}
		properties.add(new DeploymentProperty("memorySetting", Integer.class, possibleMemorySettings));
	}

	public DeploymentProperty getProperty(String key) throws SeMoDeException {
		// TODO remove this and make it a real map
		for (DeploymentProperty property : properties) {
			if (property.key.equals(key)) {
				return property;
			}
		}
		throw new SeMoDeException("not found / not null");
	}

}
