package de.uniba.dsg.serverless.calibration.local;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.LinpackParser;
import de.uniba.dsg.serverless.calibration.methods.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalCalibration implements CalibrationMethods {

    private static final FileLogger logger = ArgumentProcessor.logger;
    private static final String CONTAINER_RESULT_FOLDER = "/usr/src/linpack/output/"; // specified by linpack benchmark container
    private static final String LINPACK_DOCKERFILE = "linpack/local/Dockerfile";
    private static final String LINPACK_IMAGE = "semode/linpack";
    // composite
    private final Calibration calibration;
    private final Path temporaryLog;
    private final LocalCalibrationConfig config;

    // used for CLI feature
    public LocalCalibration(final String name, final LocalCalibrationConfig config) throws SeMoDeException {
        this.calibration = new Calibration(name, SupportedPlatform.LOCAL);
        this.temporaryLog = this.calibration.calibrationLogs.resolve("output").resolve("out.txt");
        // TODO change CLI feature here - for now - default value
        // this.steps = 0.1;
        this.config = config;
    }

    // used within pipeline
    public LocalCalibration(final String name, final Path calibrationFolder, final LocalCalibrationConfig config) throws SeMoDeException {
        this.calibration = new Calibration(name, SupportedPlatform.LOCAL, calibrationFolder);
        this.temporaryLog = this.calibration.calibrationLogs.resolve("output").resolve("out.txt");
        this.config = config;
    }

    @Override
    public void stopCalibration() {
        logger.warning("Not able to stop the local calibration!");
    }

    @Override
    public void deployCalibration() throws SeMoDeException {
        logger.info("There is no deployment for local needed");
    }

    @Override
    public void startCalibration() throws SeMoDeException {
        if (Files.exists(this.calibration.calibrationFile)) {
            logger.info("Calibration has already been performed. inspect it using \"calibrate info\"");
            return;
        }

        // prepare calibration - build container and compute quotas based on steps
        final DockerContainer linpackContainer = new DockerContainer(LINPACK_DOCKERFILE, LINPACK_IMAGE);
        linpackContainer.buildContainer();
        final int physicalCores = this.getPhysicalCores();
        logger.info("Number of cores: " + physicalCores);
        final List<Double> quotas = IntStream
                // 1.1 results from 1 + avoid rounding errors (0.1)
                .range(1, (int) (1.1 + ((double) physicalCores * 1.0 / this.config.getLocalSteps())))
                .mapToDouble(v -> this.config.getLocalSteps() * v)
                .boxed()
                .collect(Collectors.toList());

        // perform subcalibration - execute number of Calibrations N times
        final Map<Integer, List<Double>> subResults = new HashMap<>();
        for (int i = 0; i < this.config.getNumberOfLocalCalibrations(); i++) {
            subResults.put(i, this.performCalibration(i, quotas, linpackContainer));
        }

        // merge results in this.calibrationFile
        final StringBuilder stringBuilder = new StringBuilder();
        // leave first column for index of the run (for easier inspection of sub calibrations)
        stringBuilder.append(",");
        stringBuilder.append(quotas.stream().map(this.calibration.DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        for (final Integer i : subResults.keySet()) {
            stringBuilder.append("" + i + ",");
            stringBuilder.append(subResults.get(i).stream().map(String::valueOf).collect(Collectors.joining(",")));
            stringBuilder.append("\n");
        }
        try {
            Files.write(this.calibration.calibrationFile, stringBuilder.toString().getBytes());
        } catch (final IOException e) {
            throw new SeMoDeException("Could not write local calibration summary to " + this.calibration.calibrationFile.toString(), e);
        }
    }

    private List<Double> performCalibration(final int i, final List<Double> quotas, final DockerContainer linpackContainer) throws SeMoDeException {

        // Create a single sub calibration for each run
        final LocalCalibration subCalibration = new LocalCalibration(this.calibration.name + i, this.calibration.calibrationFolder, this.config);

        final List<Double> results = new ArrayList<>();
        for (final double quota : quotas) {
            logger.info("Run: " + i + " running calibration using quota " + quota);
            results.add(subCalibration.executeBenchmark(linpackContainer, quota));
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(quotas.stream().map(this.calibration.DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        stringBuilder.append(results.stream().map(String::valueOf).collect(Collectors.joining(",")));
        stringBuilder.append("\n");
        try {
            Files.write(subCalibration.calibration.calibrationFile, stringBuilder.toString().getBytes());
            // logs are maybe relevant for later usage - not deleted at this point, but maybe in future releases
//            Files.delete(temporaryLog.getParent());
        } catch (final IOException e) {
            throw new SeMoDeException("Could not write local calibration to " + subCalibration.calibration.calibrationFile.toString(), e);
        }
        return results;
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
        linpackContainer.getFilesFromContainer(CONTAINER_RESULT_FOLDER, this.calibration.calibrationLogs);
        try {
            final Path logFile = this.calibration.calibrationLogs.resolve("linpack_" + this.calibration.DOUBLE_FORMAT.format(cpuLimit) + ".log");
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