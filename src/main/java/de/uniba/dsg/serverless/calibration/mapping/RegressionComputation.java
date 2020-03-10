package de.uniba.dsg.serverless.calibration.mapping;

import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class RegressionComputation {

    private static final Logger logger = LogManager.getLogger(RegressionComputation.class.getName());

    private final Path calibrationFile;
    private final SimpleRegression regression;

    public RegressionComputation(final Path calibrationFile) {
        this.calibrationFile = calibrationFile;
        this.regression = new SimpleRegression();
    }

    public SimpleFunction computeRegression() throws SeMoDeException {
        logger.info("Compute regression for file: " + this.calibrationFile);
        try (final Reader reader = Files.newBufferedReader(this.calibrationFile)) {
            final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            final List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                throw new SeMoDeException("Corrupt calibration input file. " + this.calibrationFile);
            }
            final Map<String, String> measurements = records.get(0).toMap();
            for (final String key : measurements.keySet()) {
                this.regression.addData(Double.parseDouble(key), Double.parseDouble(measurements.get(key)));
            }
            logger.info("Pearson r: " + this.regression.getR() + " - r²: " + this.regression.getRSquare());
            return new SimpleFunction(this.regression.getSlope(), this.regression.getIntercept());
        } catch (final IOException e) {
            throw new SeMoDeException("Exception when reading " + this.calibrationFile, e);
        }
    }
}
