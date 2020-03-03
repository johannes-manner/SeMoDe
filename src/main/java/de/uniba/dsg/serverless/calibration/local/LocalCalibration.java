package de.uniba.dsg.serverless.calibration.local;

import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.calibration.LinpackParser;
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
    private static final String CONTAINER_RESULT_FOLDER = "/usr/src/linpack/output/"; // specified by linpack benchmark container
    private static final String LINPACK_DOCKERFILE = "linpack/local/Dockerfile";
    private static final String LINPACK_IMAGE = "semode/linpack";
    private final Path temporaryLog;
    // steps for the cpu quota configuration
    private final double steps;

    // used for CLI feature
    public LocalCalibration(final String name) throws SeMoDeException {
        super(name, CalibrationPlatform.LOCAL);
        this.temporaryLog = this.calibrationLogs.resolve("output").resolve("out.txt");
        // TODO change CLI feature here - for now - default value
        this.steps = 0.1;
    }

    // used within pipeline
    public LocalCalibration(final String name, final Path calibrationFolder, final double steps) throws SeMoDeException {
        super(name, CalibrationPlatform.LOCAL, calibrationFolder);
        this.temporaryLog = this.calibrationLogs.resolve("output").resolve("out.txt");
        this.steps = steps;
    }

    public void performCalibration() throws SeMoDeException {
        if (Files.exists(this.calibrationFile)) {
            logger.info("Calibration has already been performed. inspect it using \"calibrate info\"");
            return;
        }
        final DockerContainer linpackContainer = new DockerContainer(LINPACK_DOCKERFILE, LINPACK_IMAGE);
        linpackContainer.buildContainer();
        final List<Double> results = new ArrayList<>();
        final int physicalCores = this.getPhysicalCores();
        logger.info("Number of cores: " + physicalCores);
        // TODO fix me, make this part also configurable!!
        final List<Double> quotas = IntStream
                // 1.1 results from 1 + avoid rounding errors (0.1)
                .range(1, (int) (1.1 + ((double) physicalCores * 1.0 / this.steps)))
                .mapToDouble(v -> this.steps * v)
                .boxed()
                .collect(Collectors.toList());
        for (final double quota : quotas) {
            logger.info("running calibration using quota " + quota);
            results.add(this.executeBenchmark(linpackContainer, quota));
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(quotas.stream().map(this.DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        stringBuilder.append(results.stream().map(String::valueOf).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        try {
            Files.write(this.calibrationFile, stringBuilder.toString().getBytes());
            // logs are maybe relevant for later usage - not deleted at this point, but maybe in future releases
//            Files.delete(temporaryLog.getParent());
        } catch (final IOException e) {
            throw new SeMoDeException("Could not write local calibration to " + this.calibrationFile.toString(), e);
        }
    }

    /**
     * Executes runs a docker container executing the benchmark with specified resource limit.
     *
     * @param linpackContainer container
     * @param cpuLimit         todo change
     * @return average performance of linpack in GFLOPS
     * @throws SeMoDeException
     */
    private double executeBenchmark(final DockerContainer linpackContainer, final double cpuLimit) throws SeMoDeException {
        linpackContainer.startContainer(new ResourceLimit(cpuLimit, false, 0));
        final int statusCode = linpackContainer.awaitTermination();
        if (statusCode != 0) {
            throw new SeMoDeException("Benchmark failed. (status code = " + statusCode + ")");
        }
        linpackContainer.getFilesFromContainer(CONTAINER_RESULT_FOLDER, this.calibrationLogs);
        try {
            final Path logFile = this.calibrationLogs.resolve("linpack_" + this.DOUBLE_FORMAT.format(cpuLimit) + ".log");
            Files.move(this.temporaryLog, logFile);
            return new LinpackParser(logFile).parseLinpack();
        } catch (final IOException e) {
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
        final String command = "lscpu";
        final Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Core(s) per socket:")) {
                    return Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                }
            }
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
        throw new SeMoDeException("lscpu could not determin the number of cores.");
    }
}