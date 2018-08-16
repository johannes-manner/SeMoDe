package de.uniba.dsg.serverless.pipeline.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

	public final List<DeploymentProperty<?>> properties;
	public Properties rawProperties;

	public final String name;
	public final Path pathToSetup;
	public final Path pathToConfig;

	public BenchmarkSetup(String name) {
		this.name = name;
		this.pathToSetup = Paths.get(BenchmarkSetup.SETUP_LOCATION, this.name);
		this.pathToConfig = pathToSetup.resolve("settings.config");
		properties = new ArrayList<>();
		initializeProperties();
	}

	/**
	 * Note that these properties might change and must be extended in the future.
	 */
	private void initializeProperties() {
		properties.add(new DeploymentProperty<String>("language", Arrays.asList("java", "js")));
		properties.add(new DeploymentProperty<Integer>("deploymentSize"));
		// Limits: https://docs.aws.amazon.com/de_de/lambda/latest/dg/limits.html
		List<Integer> memorySettings = new ArrayList<>();
		for (int setting = 128; setting <= 3008; setting += 64) {
			memorySettings.add(setting);
		}
		properties.add(new DeploymentProperty<Integer>("memorySetting", memorySettings));
	}

}
