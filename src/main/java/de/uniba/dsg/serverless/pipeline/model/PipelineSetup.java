package de.uniba.dsg.serverless.pipeline.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

/**
 * Model class of a Pipeline Setup. A pipeline setup consists of multiple
 * benchmarks or calibrations. The settings are defined in properties file manually or
 * automatically. The format of the property file is as follows:
 * <p>
 * key=setting1,setting2,settingN
 * <p>
 * with keys being listed below.<br>
 * Resulting pipeline setup contains all possible combinations of the
 * settings.
 */
public class PipelineSetup {

    public static final String SETUP_LOCATION = "setups";
    public static final String SEPERATOR = ";";
    private static final String PIPELINE_JSON = "pipeline.json";
    private static final String SEMODE_JAR_NAME = "SeMoDe.jar";

    public final Config config;
    public final UserConfig userConfig;
    public final Map<String, ProviderConfig> possibleProviders;
    public final Map<String, ProviderConfig> userProviders;
    // name of the pipeline setup, also name for the root folder
    public final String name;
    // global pipeline paths
    public final Path pathToSetup;
    public final Path pathToConfig;
    public final Path pathToSources;
    // for benchmarking
    public final Path pathToDeployment;
    public final Path pathToEndpoints;
    public final Path pathToBenchmarkingCommands;
    public final Path pathToFetchingCommands;
    // for calibration
    public final Path pathToCalibration;

    public BenchmarkConfig benchmarkConfig;


    public PipelineSetup(final String name) throws SeMoDeException {
        this.name = name;
        this.pathToSetup = Paths.get(PipelineSetup.SETUP_LOCATION, name);
        this.pathToConfig = this.pathToSetup.resolve("settings.json");
        this.pathToSources = this.pathToSetup.resolve("sources");
        this.pathToCalibration = this.pathToSetup.resolve("calibration");

        final Path benchmarkPath = this.pathToSetup.resolve("benchmark");
        this.pathToDeployment = benchmarkPath.resolve("deployments");
        this.pathToEndpoints = benchmarkPath.resolve("endpoints");
        this.pathToBenchmarkingCommands = benchmarkPath.resolve("benchmarkingCommands");
        this.pathToFetchingCommands = benchmarkPath.resolve("fetchingCommands");

        this.benchmarkConfig = new BenchmarkConfig();
        this.userProviders = new HashMap<>();
        this.config = this.loadConfig(PIPELINE_JSON);
        this.userConfig = this.initializeUserConfig();
        this.possibleProviders = this.config.getProviderConfigMap();
    }

    /**
     * Initializes the user config with some default values which are helpful, e.g. the calibration options,
     * if some parameters should be unchanged to the global config.
     *
     * @return
     */
    private UserConfig initializeUserConfig() {
        final UserConfig userConfig = new UserConfig();
        userConfig.setCalibrationConfig(new CalibrationConfig(this.config.getCalibrationConfig()));
        return userConfig;
    }

    public boolean setupAlreadyExists() {
        return Files.exists(this.pathToSetup);
    }

    private Config loadConfig(final String path) throws SeMoDeException {
        final ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8), Config.class);
        } catch (final IOException e) {
            throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
        }
    }

    /**
     * Load the user config files.
     *
     * @return
     * @throws SeMoDeException
     */
    public void loadUserConfig(final String path) throws SeMoDeException {
        final ObjectMapper om = new ObjectMapper();
        try {
            final UserConfig config = om.readValue(Paths.get(path).toFile(), UserConfig.class);
            final Map<String, ProviderConfig> map = new HashMap<>();
            for (final ProviderConfig provider : config.getProviderConfigs()) {
                map.put(provider.getName(), provider);
            }

            // initialize all user config variables
            this.userProviders.putAll(map);
            this.benchmarkConfig = config.getBenchmarkConfig();

        } catch (final IOException e) {
            throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
        }
    }

    public void updateUserConfig() {
        final List<ProviderConfig> providerConfigs = new ArrayList<>();
        for (final ProviderConfig config : this.userProviders.values()) {
            providerConfigs.add(config);
        }
        this.userConfig.setBenchmarkConfig(this.benchmarkConfig);
        this.userConfig.setProviderConfigs(providerConfigs);
    }

    /**
     * This functions return the fully qualified name of the SeMoDe.jar file to
     * enable other utilities to generate batch files for automating the
     * benchmarking pipeline.
     *
     * @return the location of the SeMoDe.jar file to generate different batch files
     * @throws SeMoDeException if the SeMoDe.jar is not in the current project
     *                         directory.
     */
    public String getSeMoDeJarLocation() throws SeMoDeException {

        final Predicate<Path> isSeMoDeJar = p -> p.toString().endsWith(SEMODE_JAR_NAME);
        final Optional<String> jarFile;
        try {
            jarFile = Files.walk(this.pathToSetup.toAbsolutePath().getParent().getParent())
                    .filter(isSeMoDeJar)
                    .map(p -> p.toString())
                    .findFirst();

            // there is only one SeMoDe-jar
            if (!jarFile.isPresent()) {
                throw new SeMoDeException(
                        "The SeMoDe utility was not built - please execute the gradle build command before executing the command again");
            }
            return jarFile.get();
        } catch (final IOException e) {
            throw new SeMoDeException("Error while traversing the SeMoDe file tree", e);
        }
    }
}
