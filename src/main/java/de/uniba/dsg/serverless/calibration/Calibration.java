package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class Calibration {

    protected final String name;
    protected final Path calibrationFile;
    protected final Path calibrationLogs;
    protected final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###");

    /**
     * Constructor used for the CLI feature only (files are available under SeMoDe/calibration
     *
     * @param name     of the subfolder
     * @param platform platform which is tackled (used for storing the logs and the final result)
     * @throws SeMoDeException
     */
    public Calibration(final String name, final CalibrationPlatform platform) throws SeMoDeException {
        final Path calibrationFolder = Paths.get("calibration");
        this.name = name;
        this.calibrationFile = calibrationFolder.resolve(platform.getText()).resolve(name + ".csv");
        this.calibrationLogs = calibrationFolder.resolve(platform.getText()).resolve(name + "_logs");
        this.createDirectories(this.calibrationFile.getParent());
    }

    /**
     * Constructor used for pipeline setup. (files are available under SeMoDe/setupName/calibration/name)
     *
     * @param name              of the subfolder
     * @param platform
     * @param calibrationFolder folder of the calibration files
     * @throws SeMoDeException
     */
    public Calibration(final String name, final CalibrationPlatform platform, final Path calibrationFolder) throws SeMoDeException {
        this.name = name;
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
