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

    // private final Path temporaryLog = null;

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
            List<Double> results = executeBenchmark(i, client, memorySizes);
            sb.append(results.stream().map(DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
            sb.append("\n");
        }
        try {
            Files.write(calibrationFile, sb.toString().getBytes());
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
    }

    private List<Double> executeBenchmark(int i, AWSClient client, List<Integer> memorySizes) throws SeMoDeException {
        for (int memory : memorySizes) {
            client.invokeBenchmarkFunctions(memory);
        }
        List<Double> results = new ArrayList<>();
        for (int memory : memorySizes) {
            client.waitForBucketObject("" + memory, 600);
            Path log = calibrationLogs.resolve(memory + "_" + i + ".log");
            client.getFileFromBucket("" + memory, log);
            results.add(new BenchmarkParser(log).parseBenchmark());
        }
        return results;
    }

}
