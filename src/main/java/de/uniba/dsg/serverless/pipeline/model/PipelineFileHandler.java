package de.uniba.dsg.serverless.pipeline.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

/**
 * Model class of a Pipeline Setup. A pipeline setup consists of multiple benchmarks or calibrations. The settings are
 * defined in properties file manually or automatically. The format of the property file is as follows:
 * <p>
 * key=setting1,setting2,settingN
 * <p>
 * with keys being listed below.<br> Resulting pipeline setup contains all possible combinations of the settings.
 */
public class PipelineFileHandler {

//    public static final String SETUP_LOCATION = "setups";
//    private static final String PIPELINE_JSON = "pipeline.json";
//    private static final String SEMODE_JAR_NAME = "SeMoDe.jar";

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
    public FileLogger logger = null;

    public PipelineFileHandler(final String name, String setupLocation) throws SeMoDeException {
        this.name = name;
        this.pathToSetup = Paths.get(setupLocation, name);
        this.pathToConfig = this.pathToSetup.resolve("settings.json");
        this.pathToCalibration = this.pathToSetup.resolve("calibration");
        this.pathToBenchmarkExecution = this.pathToSetup.resolve("benchmark");

        this.createFolderStructure();
    }

    private void createFolderStructure() throws SeMoDeException {
        try {
            Files.createDirectories(this.pathToSetup);
            // for benchmarking
            Files.createDirectories(this.pathToBenchmarkExecution);
            // for calibration
            Files.createDirectories(this.pathToCalibration);

            this.saveUserConfigToFile(new SetupConfig(this.name));
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }

    public SetupConfig loadUserConfig() throws SeMoDeException {
        // TODO inject ObjectMapper
        final ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(this.pathToConfig.toFile(), SetupConfig.class);
        } catch (final IOException e) {
            throw new SeMoDeException("Error while parsing the " + this.name + " file. Check the config.");
        }
    }

    public void saveUserConfigToFile(SetupConfig setupConfig) throws SeMoDeException {
        try {
            new ObjectMapper().writer().withDefaultPrettyPrinter().writeValue(this.pathToConfig.toFile(), setupConfig);
        } catch (final IOException e) {
            throw new SeMoDeException("Configuration could not be saved.", e);
        }
    }

//    /**
//     * This functions return the fully qualified name of the SeMoDe.jar file to enable other utilities to generate batch
//     * files for automating the benchmarking pipeline.
//     *
//     * @return the location of the SeMoDe.jar file to generate different batch files
//     * @throws SeMoDeException if the SeMoDe.jar is not in the current project directory.
//     */
//    public String getSeMoDeJarLocation() throws SeMoDeException {
//
//        final Predicate<Path> isSeMoDeJar = p -> p.toString().endsWith(SEMODE_JAR_NAME);
//        final Optional<String> jarFile;
//        try {
//            jarFile = Files.walk(this.pathToSetup.toAbsolutePath().getParent().getParent())
//                           .filter(isSeMoDeJar)
//                           .map(p -> p.toString())
//                           .findFirst();
//
//            // there is only one SeMoDe-jar
//            if (!jarFile.isPresent()) {
//                throw new SeMoDeException(
//                        "The SeMoDe utility was not built - please execute the gradle build command before executing the command again");
//            }
//            return jarFile.get();
//        } catch (final IOException e) {
//            throw new SeMoDeException("Error while traversing the SeMoDe file tree", e);
//        }
//    }

    public FileLogger getLogger() {
        if (this.logger == null) {
            this.logger = new FileLogger("pipeline", this.pathToSetup.resolve("pipeline.log").toString(), false);
        }
        return this.logger;
    }
}
