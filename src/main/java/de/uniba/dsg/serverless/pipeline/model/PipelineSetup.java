package de.uniba.dsg.serverless.pipeline.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import de.uniba.dsg.serverless.pipeline.model.config.GlobalConfig;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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
    private static final String PIPELINE_JSON = "pipeline.json";
    private static final String SEMODE_JAR_NAME = "SeMoDe.jar";

    public final GlobalConfig globalConfig;
    // name of the pipeline setup, also name for the root folder
    public final String name;
    // global pipeline paths
    public final Path pathToSetup;
    public final Path pathToConfig;
    // for benchmarking
    public final Path pathToBenchmarkExecution;
    // for calibration
    public final Path pathToCalibration;

    // for logging the pipeline interaction
    public FileLogger logger;

    public PipelineSetup(final String name) throws SeMoDeException {
        this.name = name;
        this.pathToSetup = Paths.get(PipelineSetup.SETUP_LOCATION, name);
        this.pathToConfig = this.pathToSetup.resolve("settings.json");
        this.pathToCalibration = this.pathToSetup.resolve("calibration");
        this.pathToBenchmarkExecution = this.pathToSetup.resolve("benchmark");
        this.globalConfig = this.loadGlobalConfig(PIPELINE_JSON);
    }

    public boolean setupAlreadyExists() {
        return Files.exists(this.pathToSetup);
    }

    private GlobalConfig loadGlobalConfig(final String path) throws SeMoDeException {
        final ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8), GlobalConfig.class);
        } catch (final IOException e) {
            throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
        }
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

    public FileLogger createLogger() {
        this.logger = new FileLogger("pipeline", this.pathToSetup.resolve("pipeline.log").toString(), false);
        return this.logger;
    }
}
