package de.uniba.dsg.serverless.calibration.local;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback;
import com.github.dockerjava.core.command.BuildImageResultCallback;
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

    public final File dockerFile;
    public final String imageName;
    private final DockerClient client;

    public static final long DEFAULT_CPU_PERIOD = 100000;
    public static final long CPU_QUOTA_CONST = 100000;

    public DockerContainer(String dockerFile, String imageName) throws SeMoDeException {
        this.imageName = imageName;
        this.dockerFile = new File(dockerFile);

        if (!this.dockerFile.exists() || !this.dockerFile.isFile()) {
            throw new SeMoDeException("Dockerfile does not exist. (" + dockerFile + ")");
        }
        client = DockerClientBuilder.getInstance().build();
    }

    /**
     * Build the execution image defined in executor/Dockerfile.
     *
     * @return image id
     * @throws SeMoDeException
     */
    public String buildContainer() throws SeMoDeException {
        File baseDir = new File(".");
        try {
            imageId = client.buildImageCmd(dockerFile)
                    .withBaseDirectory(baseDir)
                    .withDockerfile(dockerFile)
                    .withTags(Collections.singleton(imageName))
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
        return startContainer(Collections.emptyMap(), ResourceLimits.unlimited());
    }


    /**
     * Starts the container with specified resources and no environment parameters.
     *
     * @param limits resource limits
     * @return container id
     */
    public String startContainer(ResourceLimits limits) {
        return startContainer(Collections.emptyMap(), limits);
    }

    /**
     * Starts the container with unlimited resources and specified environment parameters.
     *
     * @param envParams environment parameters
     * @return container id
     */
    public String startContainer(Map<String, String> envParams) {
        return startContainer(envParams, ResourceLimits.unlimited());
    }

    /**
     * Starts the container with specified resources and specified environment parameters.
     *
     * @param limits    resource limits
     * @param envParams environment parameters
     * @return container id
     */
    public String startContainer(Map<String, String> envParams, ResourceLimits limits) {
        CreateContainerResponse container = client
                .createContainerCmd(imageName)
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
     * @param limits resource limits
     * @return host config based on limits
     * @see <a href="https://github.com/docker-java/docker-java/issues/1008">https://github.com/docker-java/docker-java/issues/1008</a>
     */
    private HostConfig getHostConfig(ResourceLimits limits) {
        HostConfig config = new HostConfig();

        if (limits.cpuLimit > 0.0) {
            long cpuQuota = (long) (limits.cpuLimit * CPU_QUOTA_CONST);
            config.withCpuQuota(cpuQuota).withCpuPeriod(DEFAULT_CPU_PERIOD);
        }
        if (limits.memoryLimit > 0L) {
            config.withMemory(limits.memoryLimit);
        }
        if (limits.pinCPU) {
            config.withCpusetCpus("0");
        }
        return config;

    }

    public long getStartedAt() throws SeMoDeException {
        String startedAt = client.inspectContainerCmd(containerId)
                .exec()
                .getState()
                .getStartedAt();
        return DockerUtil.parseTime(startedAt);
    }

    /**
     * Returns the IP address of the container.
     *
     * @throws SeMoDeException if the bridge network does not exist
     */
    public String getIpAddress() throws SeMoDeException {
        ContainerNetwork bridge = client.inspectContainerCmd(containerId)
                .exec()
                .getNetworkSettings()
                .getNetworks()
                .get("bridge");
        if (bridge != null) {
            return bridge.getIpAddress();
        } else {
            throw new SeMoDeException("No bridge network found");
        }
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

    /**
     * Kills the Container
     */
    public void kill() {
        client.killContainerCmd(containerId).exec();
        client.removeContainerCmd(containerId).exec();
    }
}
