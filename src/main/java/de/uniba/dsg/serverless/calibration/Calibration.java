package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class Calibration {

    public static final Path CALIBRATION_FILES = Paths.get("calibration");

    protected final String name;
    protected final Path calibrationFile;
    protected final Path calibrationLogs;
    protected final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###");

    public Calibration(String name, CalibrationPlatform platform) throws SeMoDeException {
        calibrationFile = CALIBRATION_FILES.resolve(platform.getText()).resolve(name + ".csv");
        calibrationLogs = CALIBRATION_FILES.resolve(platform.getText()).resolve(name + "_logs");
        this.name = name;
        createDirectories(calibrationFile.getParent());
    }

    private void createDirectories(Path folder) throws SeMoDeException {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
    }
}
