package de.uniba.dsg.serverless.calibration.local;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.util.DockerUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DockerContainer {

    private String containerId;
    private String imageId;
    private final List<String> logs = new ArrayList<>();

    public final File dockerFile;
    public final String imageTag;
    private final DockerClient client;

    public static final long DEFAULT_CPU_PERIOD = 100000;
    public static final long CPU_QUOTA_CONST = 100000;

    public DockerContainer(String dockerFile, String imageTag) throws SeMoDeException {
        this.imageTag = imageTag;
        this.dockerFile = new File(dockerFile);

        if (!this.dockerFile.exists() || !this.dockerFile.isFile()) {
            throw new SeMoDeException("Dockerfile does not exist. (" + dockerFile + ")");
        }
        client = DockerClientBuilder.getInstance().build();
    }

    /**
     * Build the Docker image
     *
     * @return image id
     * @throws SeMoDeException build failed
     */
    public String buildContainer() throws SeMoDeException {
        return buildContainer(".");
    }

    /**
     * Build the Docker image
     *
     * @param baseDirectory optional, specifies the base directory
     * @return image id
     * @throws SeMoDeException build failed
     */
    public String buildContainer(String baseDirectory) throws SeMoDeException {
        File baseDir = new File(baseDirectory);
        try {
            imageId = client.buildImageCmd(dockerFile)
                    .withBaseDirectory(baseDir)
                    .withDockerfile(dockerFile)
                    .withTags(Collections.singleton(imageTag))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
            return imageId;
        } catch (DockerClientException e) {
            throw new SeMoDeException("Failed to build docker image.", e);
        }
    }

    /**
     * Starts the container with unlimited resources and no environment parameters.
     *
     * @return container id
     */
    public String startContainer() {
        return startContainer(Collections.emptyMap(), ResourceLimit.unlimited());
    }


    /**
     * Starts the container with specified resources and no environment parameters.
     *
     * @param limits resource limits
     * @return container id
     */
    public String startContainer(ResourceLimit limits) {
        return startContainer(Collections.emptyMap(), limits);
    }

    /**
     * Starts the container with unlimited resources and specified environment parameters.
     *
     * @param envParams environment parameters
     * @return container id
     */
    public String startContainer(Map<String, String> envParams) {
        return startContainer(envParams, ResourceLimit.unlimited());
    }

    /**
     * Starts the container with specified resources and specified environment parameters.
     *
     * @param limits    resource limits
     * @param envParams environment parameters
     * @return container id
     */
    public String startContainer(Map<String, String> envParams, ResourceLimit limits) {
        CreateContainerResponse container = client
                .createContainerCmd(imageTag)
                .withEnv(envParams.entrySet().stream().map(a -> a.getKey() + "=" + a.getValue()).collect(Collectors.toList()))
                .withHostConfig(getHostConfig(limits))
                .withAttachStdin(true)
                .exec();
        containerId = container.getId();
        client.startContainerCmd(containerId).exec();
        return containerId;
    }


    /**
     * Awaits the termination of the Container.
     *
     * @return status code
     */
    public int awaitTermination() {
        return client
                .waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();
    }

    /**
     * Returns the host config based on the provided settings
     *
     * @param limit resource limit
     * @return host config based on limit
     * @see <a href="https://github.com/docker-java/docker-java/issues/1008">https://github.com/docker-java/docker-java/issues/1008</a>
     */
    private HostConfig getHostConfig(ResourceLimit limit) {
        HostConfig config = new HostConfig();

        if (limit.cpuLimit > 0.0) {
            long cpuQuota = (long) (limit.cpuLimit * CPU_QUOTA_CONST);
            config.withCpuQuota(cpuQuota).withCpuPeriod(DEFAULT_CPU_PERIOD);
        }
        if (limit.memoryLimit > 0L) {
            config.withMemory(limit.memoryLimit);
        }
        if (limit.pinCPU) {
            config.withCpusetCpus("0");
        }
        return config;

    }

    // TODO any use for this?
    public long getStartedAt() throws SeMoDeException {
        String startedAt = client.inspectContainerCmd(containerId)
                .exec()
                .getState()
                .getStartedAt();
        return DockerUtil.parseTime(startedAt);
    }

    /**
     * Blocking function that retrieves all statistics (docker stats) of the container until it terminates.
     *
     * @return List of statistics
     * @throws SeMoDeException When the Thread is interrupted or
     */
    public List<Statistics> logStatistics() throws SeMoDeException {
        List<Statistics> statistics = new ArrayList<>();
        Optional<Statistics> nextRead = getNextStatistics();
        while (nextRead.isPresent()) {
            Statistics next = nextRead.get();
            statistics.add(next);
            nextRead = getNextStatistics();
        }
        return statistics;
    }

    /**
     * Blocking function that returns the current properties of the container (docker inspect)
     *
     * @return the inspect result
     * @throws SeMoDeException when the container is not found
     */
    public InspectContainerResponse inspectContainer() throws SeMoDeException {
        try {
            return client.inspectContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new SeMoDeException(e);
        }
    }

    /**
     * Blocking function that waits until the next statistics of the container is present. (docker stats)
     *
     * @return If present, statistics of next read. Empty otherwise.
     * @throws SeMoDeException Exception in callback termination.
     */
    public Optional<Statistics> getNextStatistics() throws SeMoDeException {
        AsyncResultCallback<Statistics> callback = new AsyncResultCallback<>();
        client.statsCmd(containerId).exec(callback);
        Statistics s;
        try {
            s = callback.awaitResult();
            callback.close();
        } catch (RuntimeException | IOException e) {
            throw new SeMoDeException(e);
        }
        if (s.getMemoryStats() == null || s.getMemoryStats().getUsage() == null) {
            // Container has terminated
            return Optional.empty();
        }
        return Optional.of(s);
    }

    /**
     * Returns
     * @return
     * @throws SeMoDeException
     */
    public List<String> getLogs() throws SeMoDeException {
        LogContainerCmd logContainerCmd = client.logContainerCmd(containerId);
        logContainerCmd.withStdOut(true).withStdErr(true);
        logContainerCmd.withTimestamps(true);

        try {
            List<String> logs = new ArrayList<>();
            logContainerCmd.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    logs.add(item.toString());
                }
            }).awaitCompletion();
            return logs;
        } catch (InterruptedException e) {
            throw new SeMoDeException("Could not get logs from container " + containerId + ".", e);
        }
    }

    /**
     * Copies all Files in a directory (or a single file) to the desired location.
     * <p>
     * Example Usage:<br>
     * <code>profiling.getFilesFromContainer("/app/logs/", Paths.get("logs/containerXYZ");</code>
     *
     * @param containerFolder absolute path to the folder or file inside the container
     * @param localFolder     path to folder for resulting file(s)
     * @throws SeMoDeException
     */
    public void getFilesFromContainer(String containerFolder, Path localFolder) throws SeMoDeException {
        try (TarArchiveInputStream tarStream = new TarArchiveInputStream(
                client.copyArchiveFromContainerCmd(containerId, containerFolder).exec())) {
            unTar(tarStream, localFolder);
        } catch (IOException e) {
            throw new SeMoDeException("Copying file(s) failed.", e);
        }
    }


    /**
     * Copies files to destination folder while creating subdirectories
     *
     * @param tarInputStream    input stream
     * @param destinationFolder destination
     * @throws IOException
     * @see <a href="https://github.com/docker-java/docker-java/issues/991">https://github.com/docker-java/docker-java/issues/991</a>
     */
    private void unTar(TarArchiveInputStream tarInputStream, Path destinationFolder) throws IOException {
        TarArchiveEntry tarEntry;
        while ((tarEntry = tarInputStream.getNextTarEntry()) != null) {
            if (tarEntry.isFile()) {
                Path filePath = destinationFolder.resolve(tarEntry.getName());
                Files.createDirectories(filePath.getParent());
                try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                    IOUtils.copy(tarInputStream, fos);
                }
            }
        }
        tarInputStream.close();
    }

}
