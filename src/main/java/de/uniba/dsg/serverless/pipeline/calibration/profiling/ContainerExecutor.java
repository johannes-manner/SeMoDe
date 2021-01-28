package de.uniba.dsg.serverless.pipeline.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Statistics;
import com.google.common.collect.Maps;
import de.uniba.dsg.serverless.pipeline.calibration.MemoryUnit;
import de.uniba.dsg.serverless.pipeline.calibration.local.DockerContainer;
import de.uniba.dsg.serverless.pipeline.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.RunningCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ContainerExecutor {

    private final Path pathToCalibration;
    private final DockerContainer container;
    private final MappingCalibrationConfig mappingCalibrationConfig;
    private final RunningCalibrationConfig runningCalibrationConfig;

    private Map<String, String> environmentVariables;
    private List<String> logs;

    public ContainerExecutor(final Path pathToCalibration, final MappingCalibrationConfig mappingConfig, final RunningCalibrationConfig runningConfig) throws SeMoDeException {
        this.pathToCalibration = pathToCalibration;
        this.mappingCalibrationConfig = mappingConfig;
        this.runningCalibrationConfig = runningConfig;
        this.container = new DockerContainer(this.runningCalibrationConfig.getFunctionDockerSourceFolder(), "semode/local");
        log.info("building container semode/local " + this.runningCalibrationConfig.getFunctionDockerSourceFolder());
        this.container.buildContainer();
        this.initEnvironmentVariables();
    }

    /**
     * Creates environment variables from a file.
     * Assumes that each running container has environment variables (some parameters to change the behavior).
     */
    private void initEnvironmentVariables() throws SeMoDeException {
        final Path envFile = Paths.get(this.runningCalibrationConfig.getEnvironmentVariablesFile());
        final Properties properties = new Properties();
        final Map<String, String> environmentVariables;
        try {
            properties.load(new FileInputStream(envFile.toString()));
            this.environmentVariables = Maps.fromProperties(properties);
        } catch (final IOException e) {
            throw new SeMoDeException("Could not load environment variables from " + this.runningCalibrationConfig.getEnvironmentVariablesFile() + ".", e);
        }
    }

    public void executeLocalProfiles() throws SeMoDeException {
        for (final Integer memorySize : this.mappingCalibrationConfig.getMemorySizeCPUShare().keySet()) {
            this.executeLocalProfiles(new ResourceLimit(this.mappingCalibrationConfig.getMemorySizeCPUShare().get(memorySize), false, memorySize), "" + memorySize);
        }
    }

    /**
     * Creates a profile of multiple container executions and aggregates them in a CSV file.<br>
     * Profiles are stored in /profiling/profiles/IMAGE_NAME/TIME_STAMP/
     *
     * @param limits resource limits
     * @throws SeMoDeException
     */
    private void executeLocalProfiles(final ResourceLimit limits, final String memorySize) throws SeMoDeException {
        final List<Profile> profiles = new ArrayList<>();
        final String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final Path out = this.pathToCalibration
                .resolve("profiles")
                .resolve(memorySize);
        for (int i = 0; i < this.runningCalibrationConfig.getNumberOfProfiles(); i++) {
            final Profile p = this.runContainer(this.environmentVariables, limits);
            this.saveProfile(p, out.resolve("profile_" + i + "_" + memorySize));
            profiles.add(p);
            log.info("Executed and saved profile " + i + " for memory size " + memorySize);
        }
        final String csvOutput = out.resolve("profiles.csv").toString();
        try (final CSVPrinter printer = new CSVPrinter(new FileWriter(csvOutput, true), CSVFormat.EXCEL)) {
            printer.printRecord("FunctionName", "StartTime", "EndTime", "PreciseDuration", "MemorySize", "MemoryUsed");
            for (final Profile p : profiles) {
                printer.printRecord(this.container.imageTag, p.started, p.finished, p.metaInfo.durationMS,
                        limits.getMemoryLimitInMb(), MemoryUnit.MB.fromBytes(p.metaInfo.averageMemoryUsage));
            }
        } catch (final IOException e) {
            throw new SeMoDeException("Unable to write CSV File", e);
        }
    }

    private Profile runContainer(final Map<String, String> envParams, final ResourceLimit limits) throws SeMoDeException {
        final String containerId = this.container.startContainer(envParams, limits);
        final long containerStartTime = ContainerMetrics.parseTime(this.container.inspectContainer().getState().getStartedAt());
        final List<Statistics> stats = this.container.logStatistics();
        final List<ContainerMetrics> metrics = new ArrayList<>();
        for (final Statistics s : stats) {
            metrics.add(ContainerMetrics.fromStatistics(s, containerStartTime));
        }
        final InspectContainerResponse additionalInformation = this.container.inspectContainer();
        this.logs = this.container.getLogs();
        return new Profile(metrics, additionalInformation);
    }

    private void saveProfile(final Profile profile, final Path folder) throws SeMoDeException {
        if (Files.exists(folder)) {
            throw new SeMoDeException("Folder already exists.");
        }
        profile.save(folder);
        try {
            Files.write(folder.resolve("log"), this.logs);
        } catch (final IOException e) {
            throw new SeMoDeException("Exeption writing log file.", e);
        }
    }
}
