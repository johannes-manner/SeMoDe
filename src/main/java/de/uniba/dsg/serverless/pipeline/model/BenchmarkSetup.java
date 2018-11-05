package de.uniba.dsg.serverless.pipeline.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	private static final String PIPELINE_JSON = "pipeline.json";

	public final Config config;
	public final Map<String, ProviderConfig> possibleProviders;
	public BenchmarkConfig benchmarkConfig;
	public final Map<String, ProviderConfig> userProviders;

	public final String name;
	public final Path pathToSetup;
	public final Path pathToConfig;
	public final Path pathToSources;
	public final Path pathToDeployment;
	public final Path pathToEndpoints;
	public final Path pathToBenchmarkingCommands;
	public final Path pathToFetchingCommands;

	public BenchmarkSetup(String name) throws SeMoDeException {
		this.name = name;
		this.pathToSetup = Paths.get(BenchmarkSetup.SETUP_LOCATION, name);
		this.pathToConfig = pathToSetup.resolve("settings.json");
		this.pathToSources = pathToSetup.resolve("sources");
		this.pathToDeployment = pathToSetup.resolve("deployments");
		this.pathToEndpoints = pathToSetup.resolve("endpoints");
		this.pathToBenchmarkingCommands = pathToSetup.resolve("benchmarkingCommands");
		this.pathToFetchingCommands = pathToSetup.resolve("fetchingCommands");
		this.benchmarkConfig = new BenchmarkConfig();
		this.userProviders = new HashMap<>();
		this.config = loadConfig(PIPELINE_JSON);
		this.possibleProviders = config.getProviderConfigMap();
	}

	private Config loadConfig(String path) throws SeMoDeException {
		ObjectMapper om = new ObjectMapper();
		try {
			return om.readValue(Paths.get(path).toFile(), Config.class);
		} catch (IOException e) {
			throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
		}
	}

	/**
	 * Load the user config files.
	 * 
	 * @return
	 * @throws SeMoDeException
	 */
	public void loadUserConfig(String path) throws SeMoDeException {
		ObjectMapper om = new ObjectMapper();
		try {
			UserConfig config = om.readValue(Paths.get(path).toFile(), UserConfig.class);
			Map<String, ProviderConfig> map = new HashMap<>();
			for (ProviderConfig provider : config.getProviderConfigs()) {
				map.put(provider.getName(), provider);
			}

			// initialize all user config variables
			this.userProviders.putAll(map);
			this.benchmarkConfig = config.getBenchmarkConfig();

		} catch (IOException e) {
			throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
		}
	}

	public UserConfig assembleUserConfig() {
		List<ProviderConfig> providerConfigs = new ArrayList<>();
		for (ProviderConfig config : this.userProviders.values()) {
			providerConfigs.add(config);
		}
		return new UserConfig(providerConfigs, this.benchmarkConfig);
	}
}
