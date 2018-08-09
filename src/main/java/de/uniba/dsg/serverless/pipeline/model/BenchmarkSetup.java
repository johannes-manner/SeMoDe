package de.uniba.dsg.serverless.pipeline.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

	// TODO implement this in a non redundnat way (maybe map)?
	public static final String PROVIDER_KEY = "provider";
	private List<Provider> providers = new ArrayList<>();

	public static final String MEMORY_SETTING_KEY = "awsMemorySetting";
	private List<Integer> awsMemorySettings = new ArrayList<>();

	public static final String DEPLOYMENT_SIZE_KEY = "deploymentSize";
	private List<Integer> deploymentSizes = new ArrayList<>();

	public static final String LANGUAGE_KEY = "language";
	private List<String> languages = new ArrayList<>();

	public final String name;
	public final Path pathToSetup;
	public final Path pathToConfig;

	public Properties properties;

	public BenchmarkSetup(String name) {
		this.name = name;
		this.pathToSetup = Paths.get(BenchmarkSetup.SETUP_LOCATION, this.name);
		this.pathToConfig = pathToSetup.resolve("settings.config");
	}

}
