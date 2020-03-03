package de.uniba.dsg.serverless.calibration.aws;

import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.calibration.LinpackParser;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AWSCalibration extends Calibration {

    private static final Logger logger = LogManager.getLogger(AWSCalibration.class.getName());

    // used for CLI feature
    public AWSCalibration(final String name) throws SeMoDeException {
        super(name, CalibrationPlatform.AWS);
    }

    // used within pipeline
    public AWSCalibration(final String name, final Path calibrationFolder) throws SeMoDeException {
        super(name, CalibrationPlatform.AWS, calibrationFolder);
    }

    public void performCalibration(final AWSCalibrationConfig config) throws SeMoDeException {
        if (Files.exists(this.calibrationFile)) {
            System.out.println("Provider calibration already performed.");
            return;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(config.memorySizes.stream().map(String::valueOf).collect(Collectors.joining(",")));
        sb.append("\n");

        final AWSClient client = new AWSClient(config.targetUrl, config.apiKey, config.bucketName);
        for (int i = 0; i < config.numberOfAWSExecutions; i++) {
            for (final int memory : config.memorySizes) {
                final String fileName = this.name + "/" + memory + "_" + i;
                client.invokeBenchmarkFunctions(memory, fileName);
            }
            final List<Double> results = new ArrayList<>();
            for (final int memory : config.memorySizes) {
                final String fileName = this.name + "/" + memory + "_" + i;
                client.waitForBucketObject("linpack/" + fileName, 600);
                final Path log = this.calibrationLogs.resolve(fileName);
                client.getFileFromBucket("linpack/" + fileName, log);
                results.add(new LinpackParser(log).parseLinpack());
            }
            sb.append(results.stream().map(this.DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
            sb.append("\n");
        }
        try {
            Files.write(this.calibrationFile, sb.toString().getBytes());
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }

}
