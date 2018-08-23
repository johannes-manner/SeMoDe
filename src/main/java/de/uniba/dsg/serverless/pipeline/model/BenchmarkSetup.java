package de.uniba.dsg.serverless.pipeline.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
	public final Map<String, ProviderConfig> properties;

	public final String name;
	public final Path pathToSetup;
	public final Path pathToConfig;
	public final Path pathToSources;
	public final Path pathToDeployment;
	public final Path pathToEndpoints;

	public BenchmarkSetup(String name) throws SeMoDeException {
		this.name = name;
		this.pathToSetup = Paths.get(BenchmarkSetup.SETUP_LOCATION, name);
		this.pathToConfig = pathToSetup.resolve("settings.json");
		this.pathToSources = pathToSetup.resolve("sources");
		this.pathToDeployment = pathToSetup.resolve("deployments");
		this.pathToEndpoints = pathToSetup.resolve("endpoints");
		this.properties = new HashMap<>();
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
	 * The user only specifies provider configs.
	 * 
	 * @return
	 * @throws SeMoDeException
	 */
	public Map<String, ProviderConfig> loadProviders(String path) throws SeMoDeException {
		Map<String, ProviderConfig> pMap = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		try {
			ProviderConfig[] providers = om.readValue(Paths.get(path).toFile(), ProviderConfig[].class);
			for (ProviderConfig provider : providers) {
				pMap.put(provider.getName(), provider);
			}
		} catch (IOException e) {
			throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
		}
		return pMap;
	}
}
