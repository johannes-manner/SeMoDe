package de.uniba.dsg.serverless.calibration.local;

import de.uniba.dsg.serverless.calibration.BenchmarkParser;
import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalCalibration extends Calibration {

    private static final Logger logger = LogManager.getLogger(LocalCalibration.class.getName());

    private final Path temporaryLog;

    private static final String CONTAINER_RESULT_FOLDER = "/usr/src/linpack/output/"; // specified by linpack benchmark container
    private static final String LINPACK_DOCKERFILE = "linpack/local/Dockerfile";
    private static final String LINPACK_IMAGE = "semode/linpack";

    public LocalCalibration(String name) throws SeMoDeException {
        super(name, CalibrationPlatform.LOCAL);
        temporaryLog = calibrationLogs.resolve("output").resolve("out.txt");
    }

    public void performCalibration() throws SeMoDeException {
        if (Files.exists(calibrationFile)) {
            logger.info("Calibration has already been performed. inspect it using \"calibrate info\"");
            return;
        }
        DockerContainer linpackContainer = new DockerContainer(LINPACK_DOCKERFILE, LINPACK_IMAGE);
        linpackContainer.buildContainer();
        List<Double> results = new ArrayList<>();
        int physicalCores = getPhysicalCores();
        logger.info("Number of cores: " + physicalCores);
        List<Double> quotas = IntStream
                .range(1, 1 + physicalCores * 10)
                .mapToDouble(v -> 0.1 * v)
                .boxed()
                .collect(Collectors.toList());
        for (double quota : quotas) {
            logger.info("running calibration using quota " + quota);
            results.add(executeBenchmark(linpackContainer, quota));
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(quotas.stream().map(DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        stringBuilder.append(results.stream().map(String::valueOf).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        try {
            Files.write(calibrationFile, stringBuilder.toString().getBytes());
            Files.delete(temporaryLog.getParent());
        } catch (IOException e) {
            throw new SeMoDeException("Could not write local calibration to " + calibrationFile.toString(), e);
        }
    }

    /**
     * Executes runs a docker container executing the benchmark with specified resource limit.
     *
     * @param linpackContainer container
     * @param limit            todo change
     * @return average performance of linpack in GFLOPS
     * @throws SeMoDeException
     */
    private double executeBenchmark(DockerContainer linpackContainer, double limit) throws SeMoDeException {
        linpackContainer.startContainer(new ResourceLimits(limit, false, 0));
        int statusCode = linpackContainer.awaitTermination();
        if (statusCode != 0) {
            throw new SeMoDeException("Benchmark failed. (status code = " + statusCode + ")");
        }
        linpackContainer.getFilesFromContainer(CONTAINER_RESULT_FOLDER, calibrationLogs);
        try {
            Path logFile = calibrationLogs.resolve("linpack_" + DOUBLE_FORMAT.format(limit) + ".log");
            Files.move(temporaryLog, logFile);
            return new BenchmarkParser(logFile).parseBenchmark();
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
    }

    /**
     * Returns the number of physical cores.<br>
     *
     * @return number of physical cores
     * @throws SeMoDeException when the command fails. (only supported on linux machines)
     * @see <a href="https://stackoverflow.com/questions/4759570/finding-number-of-cores-in-java">https://stackoverflow.com/questions/4759570/finding-number-of-cores-in-java</a>
     */
    private int getPhysicalCores() throws SeMoDeException {
        String command = "lscpu";
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Core(s) per socket:")) {
                    return Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                }
            }
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
        throw new SeMoDeException("lscpu could not determin the number of cores.");
    }
}