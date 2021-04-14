package de.uniba.dsg.serverless.pipeline.calibration.local;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
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

    public static final long DEFAULT_CPU_PERIOD = 100000;
    public static final long CPU_QUOTA_CONST = 100000;
    public final String dockerSourceFolder;
    public final String imageTag;
    private final List<String> logs = new ArrayList<>();
    private final DockerClient client;
    private String containerId;

    /**
     * Used when an image repository is already used, e.g. when pulling the linpack image from docker hub.
     *
     * @param imageTag
     */
    public DockerContainer(final String imageTag) throws SeMoDeException {
        this("", imageTag);
    }

    /**
     * Used when an image should be build during the pipeline procedure.
     *
     * @param dockerSourceFolder - folder where the docker files are included. Dockerfile has to be called "Dockerfile" in this folder.
     * @param imageTag
     * @throws SeMoDeException
     */
    public DockerContainer(final String dockerSourceFolder, final String imageTag) throws SeMoDeException {
        this.imageTag = imageTag;
        this.dockerSourceFolder = dockerSourceFolder;

        this.client = DockerClientBuilder.getInstance().build();
    }

    /**
     * Build the Docker image
     *
     * @return image id
     * @throws SeMoDeException build failed
     */
    public String buildContainer() throws SeMoDeException {
        try {
            String imageId = this.client.buildImageCmd()
                    .withBaseDirectory(new File(this.dockerSourceFolder))
                    .withDockerfile(new File(this.dockerSourceFolder + "/Dockerfile"))
                    .withTags(Collections.singleton(this.imageTag))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
            return imageId;
        } catch (final DockerClientException e) {
            throw new SeMoDeException("Failed to build docker image.", e);
        }
    }

    /**
     * Starts the container with unlimited resources and no environment parameters.
     *
     * @return container id
     */
    public String startContainer() {
        return this.startContainer(Collections.emptyMap(), ResourceLimit.unlimited());
    }


    /**
     * Starts the container with specified resources and no environment parameters.
     *
     * @param limits resource limits
     * @return container id
     */
    public String startContainer(final ResourceLimit limits) {
        return this.startContainer(Collections.emptyMap(), limits);
    }

    /**
     * Starts the container with unlimited resources and specified environment parameters.
     *
     * @param envParams environment parameters
     * @return container id
     */
    public String startContainer(final Map<String, String> envParams) {
        return this.startContainer(envParams, ResourceLimit.unlimited());
    }

    /**
     * Starts the container with specified resources and specified environment parameters.
     *
     * @param limits    resource limits
     * @param envParams environment parameters
     * @return container id
     */
    public String startContainer(final Map<String, String> envParams, final ResourceLimit limits) {
        final CreateContainerResponse container = this.client
                .createContainerCmd(this.imageTag)
                .withEnv(envParams.entrySet().stream().map(a -> a.getKey() + "=" + a.getValue()).collect(Collectors.toList()))
                .withHostConfig(this.getHostConfig(limits))
                .withAttachStdin(true)
                .exec();
        this.containerId = container.getId();
        this.client.startContainerCmd(this.containerId).exec();
        return this.containerId;
    }


    /**
     * Awaits the termination of the Container.
     *
     * @return status code
     */
    public int awaitTermination() {
        return this.client
                .waitContainerCmd(this.containerId)
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
    private HostConfig getHostConfig(final ResourceLimit limit) {
        final HostConfig config = new HostConfig();

        if (limit.cpuLimit > 0.0) {
            final long cpuQuota = (long) (limit.cpuLimit * CPU_QUOTA_CONST);
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

    /**
     * Blocking function that retrieves all statistics (docker stats) of the container until it terminates.
     *
     * @return List of statistics
     * @throws SeMoDeException When the Thread is interrupted or
     */
    public List<Statistics> logStatistics() throws SeMoDeException {
        final List<Statistics> statistics = new ArrayList<>();
        Optional<Statistics> nextRead = this.getNextStatistics();
        while (nextRead.isPresent()) {
            final Statistics next = nextRead.get();
            statistics.add(next);
            nextRead = this.getNextStatistics();
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
            return this.client.inspectContainerCmd(this.containerId).exec();
        } catch (final NotFoundException e) {
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
        final AsyncResultCallback<Statistics> callback = new AsyncResultCallback<>();
        this.client.statsCmd(this.containerId).exec(callback);
        final Statistics s;
        try {
            s = callback.awaitResult();
            callback.close();
        } catch (final RuntimeException | IOException e) {
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
     *
     * @return
     * @throws SeMoDeException
     */
    public List<String> getLogs() throws SeMoDeException {
        final LogContainerCmd logContainerCmd = this.client.logContainerCmd(this.containerId);
        logContainerCmd.withStdOut(true).withStdErr(true);
        logContainerCmd.withTimestamps(true);

        try {
            final List<String> logs = new ArrayList<>();
            logContainerCmd.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(final Frame item) {
                    logs.add(item.toString());
                }
            }).awaitCompletion();
            return logs;
        } catch (final InterruptedException e) {
            throw new SeMoDeException("Could not get logs from container " + this.containerId + ".", e);
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
    public void getFilesFromContainer(final String containerFolder, final Path localFolder) throws SeMoDeException {
        try (final TarArchiveInputStream tarStream = new TarArchiveInputStream(
                this.client.copyArchiveFromContainerCmd(this.containerId, containerFolder).exec())) {
            this.unTar(tarStream, localFolder);
        } catch (final IOException e) {
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
    private void unTar(final TarArchiveInputStream tarInputStream, final Path destinationFolder) throws IOException {
        TarArchiveEntry tarEntry;
        while ((tarEntry = tarInputStream.getNextTarEntry()) != null) {
            if (tarEntry.isFile()) {
                final Path filePath = destinationFolder.resolve(tarEntry.getName());
                Files.createDirectories(filePath.getParent());
                try (final FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                    IOUtils.copy(tarInputStream, fos);
                }
            }
        }
        tarInputStream.close();
    }

}
