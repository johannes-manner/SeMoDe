package de.uniba.dsg.serverless.pipeline.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Statistics;
import com.google.common.collect.Maps;
import de.uniba.dsg.serverless.pipeline.calibration.MemoryUnit;
import de.uniba.dsg.serverless.pipeline.calibration.local.DockerContainer;
import de.uniba.dsg.serverless.pipeline.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.pipeline.model.config.RunningCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ContainerExecutor {

    private final Path pathToCalibration;
    private final List<DockerContainer> containers;
    private final Map<Integer, Double> memorySizeCPUShare;
    private final RunningCalibrationConfig runningCalibrationConfig;

    private Map<String, String> environmentVariables;
    private List<String> logs;

    public ContainerExecutor(final Path pathToCalibration, Map<Integer, Double> memorySizeCPUShare, final RunningCalibrationConfig runningConfig) throws SeMoDeException {
        this.pathToCalibration = pathToCalibration;
        this.memorySizeCPUShare = memorySizeCPUShare;
        this.runningCalibrationConfig = runningConfig;
        this.containers = new ArrayList<>();

        // build a number of containers for simulating also the microservice use case
        int i = 0;
        for (String sourceFolder : this.runningCalibrationConfig.getFunctionDockerSourceFolder().split(",")) {
            DockerContainer container = new DockerContainer(sourceFolder, "semode/local_" + i);
            log.info("building container semode/local " + sourceFolder);
            container.buildContainer();
            this.containers.add(container);
        }

        this.initEnvironmentVariables();
    }

    /**
     * Creates environment variables from a file.
     * Assumes that each running container has environment variables (some parameters to change the behavior).
     */
    private void initEnvironmentVariables() throws SeMoDeException {
        final Path envFile = Paths.get(this.runningCalibrationConfig.getEnvironmentVariablesFile());
        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(envFile.toString()));
            this.environmentVariables = Maps.fromProperties(properties);
        } catch (final IOException e) {
            throw new SeMoDeException("Could not load environment variables from " + this.runningCalibrationConfig.getEnvironmentVariablesFile() + ".", e);
        }
    }

    public List<ProfileRecord> executeLocalProfiles() throws SeMoDeException {
        String randomExecutionNumber = UUID.randomUUID().toString();
        List<ProfileRecord> profileRecords = new ArrayList<>();
        for (final Integer memorySize : this.memorySizeCPUShare.keySet()) {
            profileRecords.addAll(
                    this.executeLocalProfiles(randomExecutionNumber,
                            new ResourceLimit(this.memorySizeCPUShare.get(memorySize),
                                    false, memorySize),
                            "" + memorySize));
        }
        return profileRecords;
    }

    /**
     * TODO document
     */
    private List<ProfileRecord> executeLocalProfiles(final String randomExecutionNumber, final ResourceLimit limits, final String memorySize) throws SeMoDeException {
        final List<Profile> profiles = new ArrayList<>();
        final String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final Path out = this.pathToCalibration
                .resolve("profiles")
                .resolve(memorySize);

        for (int i = 0; i < this.runningCalibrationConfig.getNumberOfProfiles(); i++) {
            for (int j = 0; j < this.containers.size(); j++) {
                final Profile p = this.runContainer(this.environmentVariables, limits, this.containers.get(j), "Container_" + j);
                this.saveProfile(p, out.resolve("profile_" + randomExecutionNumber + "_" + i + "_" + memorySize + "_container_" + j));
                profiles.add(p);
                log.info("Executed profile " + i + " for memory size " + memorySize + " and container " + this.containers.get(j).imageTag);
            }
        }

        List<ProfileRecord> records = new ArrayList<>();

        for (final Profile p : profiles) {
            records.add(new ProfileRecord(randomExecutionNumber,
                    p.containerName,
                    p.started,
                    p.finished,
                    p.metaInfo.durationMS,
                    limits.getMemoryLimitInMb(),
                    MemoryUnit.MB.fromBytes(p.metaInfo.averageMemoryUsage)));
        }

        return records;
    }

    private Profile runContainer(final Map<String, String> envParams, final ResourceLimit limits, DockerContainer container, final String containerName) throws SeMoDeException {
        final String containerId = container.startContainer(envParams, limits);
        final long containerStartTime = ContainerMetrics.parseTime(container.inspectContainer().getState().getStartedAt());
        final List<Statistics> stats = container.logStatistics();
        final List<ContainerMetrics> metrics = new ArrayList<>();
        for (final Statistics s : stats) {
            metrics.add(ContainerMetrics.fromStatistics(s, containerStartTime));
        }
        final InspectContainerResponse additionalInformation = container.inspectContainer();
        this.logs = container.getLogs();
        return new Profile(metrics, additionalInformation, containerName);
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
