package de.uniba.dsg.serverless.calibration.aws;

import de.uniba.dsg.serverless.calibration.BenchmarkParser;
import de.uniba.dsg.serverless.calibration.Calibration;
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

    public AWSCalibration(String name) throws SeMoDeException {
        super(name);
    }

    public void performCalibration(
            String targetUrl,
            String apiKey,
            String bucketName,
            List<Integer> memorySizes,
            int numberOfCalibrations) throws SeMoDeException {
        if (Files.exists(calibrationFile)) {
            System.out.println("Provider calibration already performed.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(memorySizes.stream().map(String::valueOf).collect(Collectors.joining(",")));
        sb.append("\n");

        AWSClient client = new AWSClient(targetUrl, apiKey, bucketName);
        for (int i = 0; i < numberOfCalibrations; i++) {
            for (int memory : memorySizes) {
                String fileName = name + "/" + memory + "_" + i;
                client.invokeBenchmarkFunctions(memory, fileName);
            }
            List<Double> results = new ArrayList<>();
            for (int memory : memorySizes) {
                String fileName = name + "/" + memory + "_" + i;
                client.waitForBucketObject("linpack/" + fileName, 600);
                Path log = calibrationLogs.resolve(fileName);
                client.getFileFromBucket("linpack/" + fileName, log);
                results.add(new BenchmarkParser(log).parseBenchmark());
            }
            sb.append(results.stream().map(DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
            sb.append("\n");
        }
        try {
            Files.write(calibrationFile, sb.toString().getBytes());
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
    }

}
