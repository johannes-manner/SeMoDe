package de.uniba.dsg.serverless.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Statistics;
import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.calibration.MemoryUnit;
import de.uniba.dsg.serverless.calibration.local.DockerContainer;
import de.uniba.dsg.serverless.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.cli.CalibrationUtility;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerExecutor {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private final DockerContainer container;
    private List<String> logs;

    public ContainerExecutor(final String containerTag, final String dockerFile, final boolean buildContainer) throws SeMoDeException {
        // TODO maybe change so context is passed here instead of dockerfile -> Dockerfile by default
        this.container = new DockerContainer(dockerFile, containerTag);
        if (buildContainer) {
            logger.info("building container " + containerTag);
            this.container.buildContainer();
        }
    }

    /**
     * Creates a profile of multiple container executions and aggregates them in a CSV file.<br>
     * Profiles are stored in /profiling/profiles/IMAGE_NAME/TIME_STAMP/
     *
     * @param environmentVariables environment variables for docker container
     * @param limits               resource limits
     * @param n                    number of executions
     * @throws SeMoDeException
     */
    public void executeLocalProfiles(final Map<String, String> environmentVariables, final ResourceLimit limits, final int n) throws SeMoDeException {
        final List<Profile> profiles = new ArrayList<>();
        final String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final Path out = CalibrationUtility.PROFILING_PATH
                .resolve("profiles")
                .resolve(this.container.imageTag.split("/")[1]) // use the name of docker tag (<org>/<name> -> <name>)
                .resolve(time);
        for (int i = 0; i < n; i++) {
            final Profile p = this.runContainer(environmentVariables, limits);
            this.saveProfile(p, out.resolve("profile_" + i));
            profiles.add(p);
            logger.info("Executed and saved profile " + i);
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
