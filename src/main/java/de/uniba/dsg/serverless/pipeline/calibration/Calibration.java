package de.uniba.dsg.serverless.pipeline.calibration;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class Calibration {

    public final String name;
    public final Path calibrationFile;
    public final Path calibrationLogs;
    public final Path calibrationFolder;
    public final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###");

    /**
     * Constructor used for the CLI feature only (files are available under SeMoDe/calibration
     *
     * @param name     of the subfolder
     * @param platform platform which is tackled (used for storing the logs and the final result)
     */
    // TODO needed??
    public Calibration(final String name, final CalibrationPlatform platform) throws SeMoDeException {
        this.calibrationFolder = Paths.get("calibration");
        this.name = name;
        this.calibrationFile = this.calibrationFolder.resolve(platform.getText()).resolve(name + ".csv");
        this.calibrationLogs = this.calibrationFolder.resolve(platform.getText()).resolve(name + "_logs");
        this.createDirectories(this.calibrationFile.getParent());
    }

    /**
     * Constructor used for pipeline setup. (files are available under SeMoDe/setupName/calibration/name)
     *
     * @param name              of the subfolder
     * @param calibrationFolder folder of the calibration files
     */
    public Calibration(final String name, final CalibrationPlatform platform, final Path calibrationFolder) {
        this.name = name;
        this.calibrationFolder = calibrationFolder;
        this.calibrationFile = calibrationFolder.resolve(platform.getText()).resolve(name + ".csv");
        this.calibrationLogs = calibrationFolder.resolve(platform.getText()).resolve(name + "_logs");
    }

    private void createDirectories(final Path folder) throws SeMoDeException {
        try {
            Files.createDirectories(folder);
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }
}
