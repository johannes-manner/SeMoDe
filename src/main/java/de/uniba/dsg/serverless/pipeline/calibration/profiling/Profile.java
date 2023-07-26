package de.uniba.dsg.serverless.pipeline.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Profile {
    public final List<ContainerMetrics> metrics;
    public final List<ContainerMetrics> deltaMetrics;
    public final InspectContainerResponse additional;
    public final ContainerMetrics lastMetrics;
    public final LocalDateTime started;
    public final LocalDateTime finished;
    public final ProfileMetaInfo metaInfo;

    public final String containerName;

    public Profile(final List<ContainerMetrics> metrics, final InspectContainerResponse additional, String containerName) throws SeMoDeException {
        this.validateMetrics(metrics);
        this.validateAdditionalInformation(additional);
        this.metrics = metrics;
        this.deltaMetrics = this.calculateDeltaMetrics();
        this.additional = additional;
        this.lastMetrics = metrics.get(metrics.size() - 1);

        try {
            this.started = LocalDateTime.parse(additional.getState().getStartedAt(), DateTimeFormatter.ISO_DATE_TIME);
            this.finished = LocalDateTime.parse(additional.getState().getFinishedAt(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (final DateTimeParseException e) {
            throw new SeMoDeException(e);
        }
        this.metaInfo = new ProfileMetaInfo(this);
        this.containerName = containerName;
    }

    private List<ContainerMetrics> calculateDeltaMetrics() throws SeMoDeException {
        final List<ContainerMetrics> l = new ArrayList<>();
        l.add(this.metrics.get(0));
        l.addAll(IntStream.range(0, this.metrics.size() - 1)
                .mapToObj(i -> new ContainerMetrics(this.metrics.get(i), this.metrics.get(i + 1)))
                .collect(Collectors.toList()));
        return l;
    }

    /**
     * Saves the metrics (docker stats) as a CSV and the additiona information (docker inspect) as a JSON.
     *
     * @param folder folder
     * @throws SeMoDeException If an IO Exception is thrown
     */
    public void save(final Path folder) throws SeMoDeException {
        try {
            Files.createDirectories(folder);
            this.saveCSV(folder);
            this.saveAdditionalInformation(folder);
        } catch (final IOException e) {
            throw new SeMoDeException("Error saving profile to folder " + folder.toString(), e);
        }
    }

    private void saveCSV(final Path folder) throws IOException {
        final List<String> lines = new ArrayList<>();
        lines.add(this.getHeader());         // header
        lines.add(this.getEmptyMetrics());   // adds empty metrics with (<start time>,0,0,..)
        for (final ContainerMetrics metric : this.metrics) {
            lines.add(metric.formatCSVLine());
        }
        Files.write(folder.resolve("metrics.csv"), lines);
    }

    private void saveAdditionalInformation(final Path folder) throws IOException, SeMoDeException {
        final Gson jsonParser = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Files.write(folder.resolve("meta.json"), jsonParser.toJson(this.metaInfo).getBytes());
    }

    private String getHeader() {
        final List<String> headerEntries = new ArrayList<>();
        headerEntries.add("timeStamp");
        headerEntries.addAll(this.metrics.get(0).relevantMetrics);
        return String.join(",", headerEntries);
    }

    private String getEmptyMetrics() {
        return this.additional.getState().getStartedAt() + StringUtils.repeat(",0", this.metrics.get(0).relevantMetrics.size());
    }

    private void validateMetrics(final List<ContainerMetrics> metrics) throws SeMoDeException {
        if (metrics == null || metrics.isEmpty()) {
            throw new SeMoDeException("Metrics must be a non empty List.");
        }
    }

    private void validateAdditionalInformation(final InspectContainerResponse additional) throws SeMoDeException {
        final int exitCode = Optional
                .ofNullable(additional)
                .map(InspectContainerResponse::getState)
                .map(InspectContainerResponse.ContainerState::getExitCode)
                .orElseThrow(() -> new SeMoDeException("Exit code not present."));
        if (exitCode != 0 || !"exited".equals(additional.getState().getStatus())) {
            final String exit = "exit code: " + exitCode;
            final String status = "status: " + additional.getState().getStatus();
            throw new SeMoDeException(exit + " - " + status);
        }
    }

}
