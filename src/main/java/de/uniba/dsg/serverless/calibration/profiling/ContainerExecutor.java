package de.uniba.dsg.serverless.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Statistics;
import de.uniba.dsg.serverless.calibration.MemoryUnit;
import de.uniba.dsg.serverless.calibration.local.DockerContainer;
import de.uniba.dsg.serverless.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.cli.CalibrationUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger logger = LogManager.getLogger(ContainerExecutor.class.getName());
    private final DockerContainer container;
    private List<String> logs;

    public ContainerExecutor(String containerTag, String dockerFile, boolean buildContainer) throws SeMoDeException {
        // TODO maybe change so context is passed here instead of dockerfile -> Dockerfile by default
        container = new DockerContainer(dockerFile, containerTag);
        if (buildContainer) {
            logger.info("building container " + containerTag);
            container.buildContainer();
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
    public void executeLocalProfiles(Map<String, String> environmentVariables, ResourceLimit limits, int n) throws SeMoDeException {
        List<Profile> profiles = new ArrayList<>();
        String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Path out = CalibrationUtility.PROFILING_PATH
                .resolve("profiles")
                .resolve(container.imageTag.split("/")[1]) // use the name of docker tag (<org>/<name> -> <name>)
                .resolve(time);
        for (int i = 0; i < n; i++) {
            Profile p = runContainer(environmentVariables, limits);
            saveProfile(p, out.resolve("profile_" + i));
            profiles.add(p);
            logger.info("Executed and saved profile " + i);
        }
        String csvOutput = out.resolve("profiles.csv").toString();
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(csvOutput, true), CSVFormat.EXCEL)) {
            printer.printRecord("FunctionName", "StartTime", "EndTime", "PreciseDuration", "MemorySize", "MemoryUsed");
            for (Profile p : profiles) {
                printer.printRecord(container.imageTag, p.started, p.finished, p.metaInfo.durationMS,
                        limits.getMemoryLimitInMb(), MemoryUnit.MB.fromBytes(p.metaInfo.averageMemoryUsage));
            }
        } catch (IOException e) {
            throw new SeMoDeException("Unable to write CSV File", e);
        }
    }

    private Profile runContainer(Map<String, String> envParams, ResourceLimit limits) throws SeMoDeException {
        String containerId = container.startContainer(envParams, limits);
        long containerStartTime = ContainerMetrics.parseTime(container.inspectContainer().getState().getStartedAt());
        List<Statistics> stats = container.logStatistics();
        List<ContainerMetrics> metrics = new ArrayList<>();
        for (Statistics s : stats) {
            metrics.add(ContainerMetrics.fromStatistics(s, containerStartTime));
        }
        InspectContainerResponse additionalInformation = container.inspectContainer();
        logs = container.getLogs();
        return new Profile(metrics, additionalInformation);
    }

    private void saveProfile(Profile profile, Path folder) throws SeMoDeException {
        if (Files.exists(folder)) {
            throw new SeMoDeException("Folder already exists.");
        }
        profile.save(folder);
        try {
            Files.write(folder.resolve("log"), logs);
        } catch (IOException e) {
            throw new SeMoDeException("Exeption writing log file.", e);
        }
    }

}
