package de.uniba.dsg.serverless.calibration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.uniba.dsg.serverless.util.SeMoDeException;

/**
 * Class to only parse the Linpack results locally and on providers' platfrom. See {@link #parseLinpack()}.
 *
 * @author mendress
 */
public class LinpackParser {

    private final Path linpackCalibrationPath;

    public LinpackParser(final Path linpackCalibrationPath) {
        this.linpackCalibrationPath = linpackCalibrationPath;
    }

    /**
     * Parses the Linpack benchmark/calibration result due to the fixed structure of the Linpack output written to the
     * logFiles.
     *
     * @return the average GFLOPS performance for 10000 equations to solve with an array of 25000 leading dimensions.
     * @throws SeMoDeException if the file can't be read.
     */
    public double parseLinpack() throws SeMoDeException {
        if (!Files.exists(this.linpackCalibrationPath)) {
            throw new SeMoDeException("Calibration benchmark file does not exist.");
        }
        try {
            final List<String> lines = Files.readAllLines(this.linpackCalibrationPath);
            final String[] results = lines.get(lines.size() - 7).split("\\s+");
            return Double.parseDouble(results[3]);
        } catch (final IOException e) {
            throw new SeMoDeException("Could not read file. ", e);
        }
    }
}
