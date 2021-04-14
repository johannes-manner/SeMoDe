package de.uniba.dsg.serverless.pipeline.calibration.local;

import de.uniba.dsg.serverless.pipeline.calibration.Calibration;
import de.uniba.dsg.serverless.pipeline.calibration.LinpackParser;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.calibration.model.GflopsExecutionTime;
import de.uniba.dsg.serverless.pipeline.calibration.provider.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.LocalCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class LocalCalibration implements CalibrationMethods {

    private static final String CONTAINER_RESULT_FOLDER = "/usr/src/linpack/output/"; // specified by linpack benchmark container
    private static final String LINPACK_IMAGE = "semode/linpack";
    // composite
    private final Calibration calibration;
    private final Path temporaryLog;
    private final LocalCalibrationConfig config;

    /**
     * Used for the CLI feature when executing the hardware calibration.
     *
     * @param name
     * @param config
     * @throws SeMoDeException
     */
    public LocalCalibration(String name, final LocalCalibrationConfig config) throws SeMoDeException {
        this.calibration = new Calibration(name, CalibrationPlatform.LOCAL);
        this.temporaryLog = this.calibration.calibrationLogs.resolve("output").resolve("out.txt");
        this.config = config;
    }

    /**
     * used within pipeline, when the calibration is executed there.
     * Depends on a setup.
     */
    public LocalCalibration(final String name, final Path calibrationFolder, final LocalCalibrationConfig config) {
        this.calibration = new Calibration(name, CalibrationPlatform.LOCAL, calibrationFolder);
        this.temporaryLog = this.calibration.calibrationLogs.resolve("output").resolve("out.txt");
        this.config = config;
    }

    @Override
    public void undeployCalibration() {
        log.warn("Not able to stop the local calibration!");
    }

    @Override
    public void deployCalibration() {
        log.info("There is no deployment for local needed");
    }

    @Override
    public List<CalibrationEvent> startCalibration() throws SeMoDeException {
        if (Files.exists(this.calibration.calibrationFile)) {
            log.info("Calibration has already been performed.");
            return List.of();
        }

        // prepare calibration - build container and compute quotas based on steps
        final DockerContainer linpackContainer = new DockerContainer(this.config.getCalibrationDockerSourceFolder(), LINPACK_IMAGE);
        linpackContainer.buildContainer();
        final List<CalibrationEvent> subResults = this.executeCalibrations(linpackContainer);

        return subResults;
    }

    public List<CalibrationEvent> startCliHardwareCalibration(String dockerRepository) throws SeMoDeException {
        final DockerContainer linpackContainer = new DockerContainer(dockerRepository);
        return this.executeCalibrations(linpackContainer);
    }

    private List<CalibrationEvent> executeCalibrations(DockerContainer linpackContainer) throws SeMoDeException {
        final int physicalCores = this.getPhysicalCores();
        log.info("Number of cores: " + physicalCores);
        final List<Double> quotas = IntStream
                // 1.1 results from 1 + avoid rounding errors (0.1)
                .range(1, (int) (1.1 + ((double) physicalCores * 1.0 / this.config.getLocalSteps())))
                .mapToDouble(v -> this.config.getLocalSteps() * v)
                .boxed()
                .collect(Collectors.toList());

        // perform subcalibration - execute number of Calibrations N times
        final List<CalibrationEvent> subResults = new ArrayList<>();
        for (int i = 0; i < this.config.getNumberOfLocalCalibrations(); i++) {
            subResults.addAll(i, this.performCalibration(i, quotas, linpackContainer));
        }
        return subResults;
    }

    /**
     * Executes the linpack functionality once for each quota in the quotas list!
     *
     * @param i                Number of the run
     * @param quotas           List of quotas for which the local calibration should be executed
     * @param linpackContainer docker container, where the linpack functionality is included
     * @return
     * @throws SeMoDeException
     */
    private List<CalibrationEvent> performCalibration(final int i, final List<Double> quotas, final DockerContainer linpackContainer) throws SeMoDeException {

        // Create a single sub calibration for each run
        final LocalCalibration subCalibration = new LocalCalibration(this.calibration.name + i, this.calibration.calibrationFolder, this.config);

        final List<CalibrationEvent> results = new ArrayList<>();
        for (final double quota : quotas) {
            log.info("Run: " + i + " running calibration using quota " + quota);
            results.add(new CalibrationEvent(i, quota, subCalibration.executeBenchmark(linpackContainer, quota), CalibrationPlatform.LOCAL));
        }
        return results;
    }

    /**
     * Executes runs a docker container executing the benchmark with specified resource limit.
     *
     * @param linpackContainer container
     * @param cpuLimit         todo change
     * @return average performance of linpack in GFLOPS
     */
    private GflopsExecutionTime executeBenchmark(final DockerContainer linpackContainer, final double cpuLimit) throws SeMoDeException {

        final Path logFile = this.calibration.calibrationLogs.resolve("linpack_" + this.calibration.DOUBLE_FORMAT.format(cpuLimit) + ".log");
        if (Files.exists(logFile)) {
            log.info("Local calibration already exists for " + cpuLimit);
            return new LinpackParser(logFile).parseLinpack();
        } else {
            log.info("Start execution...");
            linpackContainer.startContainer(new ResourceLimit(cpuLimit, false, 0));
            final int statusCode = linpackContainer.awaitTermination();
            if (statusCode != 0) {
                throw new SeMoDeException("Benchmark failed. (status code = " + statusCode + ")");
            }
            linpackContainer.getFilesFromContainer(CONTAINER_RESULT_FOLDER, this.calibration.calibrationLogs);
            try {

                Files.move(this.temporaryLog, logFile);
                return new LinpackParser(logFile).parseLinpack();
            } catch (final IOException e) {
                throw new SeMoDeException(e);
            }
        }
    }

    /**
     * Returns the number of physical cores.<br>
     *
     * @return number of physical cores
     * @throws SeMoDeException when the command fails. (only supported on linux machines)
     * @see <a href="https://stackoverflow.com/questions/4759570/finding-number-of-cores-in-java">https://stackoverflow.com/questions/4759570/finding-number-of-cores-in-java</a>
     */
    // TODO put this method in another class
    public static int getPhysicalCores() throws SeMoDeException {
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