package de.uniba.dsg.serverless.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Statistics;
import de.uniba.dsg.serverless.calibration.local.DockerContainer;
import de.uniba.dsg.serverless.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerExecutor {

    private static final Logger logger = LogManager.getLogger(ContainerExecutor.class.getName());
    private final DockerContainer container;
    private String containerId;
    private Profile profile;
    private List<String> logs;

    public ContainerExecutor(String containerTag, String dockerFile, boolean buildContainer) throws SeMoDeException {
        // TODO maybe change so context is passed here instead of dockerfile -> Dockerfile by default
        container = new DockerContainer(dockerFile, containerTag);
        if (buildContainer) {
            logger.info("building container " + containerTag);
            container.buildContainer();
        }
    }

    public void runContainer(Map<String, String> envParams, ResourceLimit limits) throws SeMoDeException {
        containerId = container.startContainer(envParams, limits);
        long containerStartTime = ContainerMetrics.parseTime(container.inspectContainer().getState().getStartedAt());
        List<Statistics> stats = container.logStatistics();
        List<ContainerMetrics> metrics = new ArrayList<>();
        for (Statistics s : stats) {
            metrics.add(ContainerMetrics.fromStatistics(s, containerStartTime));
        }
        InspectContainerResponse additionalInformation = container.inspectContainer();
        logs = container.getLogs();
        profile = new Profile(metrics, additionalInformation);
    }

    public void saveProfile(Path folder) throws SeMoDeException {
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
