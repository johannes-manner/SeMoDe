package de.uniba.dsg.serverless.pipeline.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Model class of a Pipeline Setup. A pipeline setup consists of multiple benchmarks or calibrations. The settings are
 * defined in properties file manually or automatically. The format of the property file is as follows:
 * <p>
 * key=setting1,setting2,settingN
 * <p>
 * with keys being listed below.<br> Resulting pipeline setup contains all possible combinations of the settings.
 */
@Slf4j
public class PipelineFileHandler {

    // name of the pipeline setup, also name for the root folder
    public final String name;
    // global pipeline paths
    public final Path pathToSetup;
    // for benchmarking
    public final Path pathToBenchmarkExecution;
    // for calibration
    public final Path pathToCalibration;

    public PipelineFileHandler(final String name, String setupLocation) throws SeMoDeException {
        this.name = name;
        this.pathToSetup = Paths.get(setupLocation, name);
        this.pathToCalibration = this.pathToSetup.resolve("calibration");
        this.pathToBenchmarkExecution = this.pathToSetup.resolve("benchmark");

        if (Files.exists(this.pathToSetup) == false) {
            this.createFolderStructure();
        }
    }

    private void createFolderStructure() throws SeMoDeException {
        try {
            Files.createDirectories(this.pathToSetup);
            // for benchmarking
            Files.createDirectories(this.pathToBenchmarkExecution);
            // for calibration
            Files.createDirectories(this.pathToCalibration);

        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }

}
