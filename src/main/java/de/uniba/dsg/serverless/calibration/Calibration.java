package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Calibration {

    private static final Logger logger = LogManager.getLogger(Calibration.class.getName());

    private CalibrationPlatform platform;
    private final Path calibrationFile;
    private final Path temporaryLog;
    private final Path calibrationLogs;

    private static final Path CALIBRATION_FILES = Paths.get("calibration");
    private static final String CONTAINER_RESULT_FOLDER = "/usr/src/linpack/output/"; // specified by linpack benchmark container
    private static final String LINPACK_DOCKERFILE = "linpack/local/Dockerfile";
    private static final String LINPACK_IMAGE = "semode/linpack";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    public Calibration(CalibrationPlatform platform, String name) throws SeMoDeException {
        this.platform = platform;
        calibrationFile = CALIBRATION_FILES.resolve(platform.getText()).resolve(name + ".csv");
        calibrationLogs = CALIBRATION_FILES.resolve(platform.getText()).resolve(name + "_logs");
        temporaryLog = calibrationLogs.resolve("output").resolve("out.txt");
        createDirectories(calibrationFile.getParent());
    }

    private void createDirectories(Path folder) throws SeMoDeException {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }
    }

    public void calibrate() throws SeMoDeException {
        switch (platform) {
            case LOCAL:
                performLocalCalibration();
                break;
            case AWS:
                break;
        }
    }

    private void performLocalCalibration() throws SeMoDeException {
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
        quotas = Arrays.asList(1.2, 1.6);
        for (double quota : quotas) {
            logger.info("running calibration using quota " + quota);
            results.add(executeLocalBenchmark(linpackContainer, quota));
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(quotas.stream().map(DECIMAL_FORMAT::format).collect(Collectors.joining(",")));
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
    private double executeLocalBenchmark(DockerContainer linpackContainer, double limit) throws SeMoDeException {
        linpackContainer.startContainer(new ResourceLimits(limit, false, 0));
        int statusCode = linpackContainer.awaitTermination();
        if (statusCode != 0) {
            throw new SeMoDeException("Benchmark failed. (status code = " + statusCode + ")");
        }
        linpackContainer.getFilesFromContainer(CONTAINER_RESULT_FOLDER, calibrationLogs);
        try {
            Path logFile = calibrationLogs.resolve("linpack_" + DECIMAL_FORMAT.format(limit) + ".log");
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