package de.uniba.dsg.serverless.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uniba.dsg.serverless.model.SeMoDeException;
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

    public Profile(List<ContainerMetrics> metrics, InspectContainerResponse additional) throws SeMoDeException {
        validateMetrics(metrics);
        validateAdditionalInformation(additional);
        this.metrics = metrics;
        this.deltaMetrics = calculateDeltaMetrics();
        this.additional = additional;
        this.lastMetrics = metrics.get(metrics.size() - 1); // FIXME

        try {
            started = LocalDateTime.parse(additional.getState().getStartedAt(), DateTimeFormatter.ISO_DATE_TIME);
            finished = LocalDateTime.parse(additional.getState().getFinishedAt(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new SeMoDeException(e);
        }
        metaInfo = new ProfileMetaInfo(this);
    }

    private List<ContainerMetrics> calculateDeltaMetrics() throws SeMoDeException {
        List<ContainerMetrics> l = new ArrayList<>();
        l.add(metrics.get(0));
        l.addAll(IntStream.range(0, metrics.size() - 1)
                .mapToObj(i -> new ContainerMetrics(metrics.get(i), metrics.get(i + 1)))
                .collect(Collectors.toList()));
        return l;
    }

    /**
     * Saves the metrics (docker stats) as a CSV and the additiona information (docker inspect) as a JSON.
     *
     * @param folder folder
     * @throws SeMoDeException If an IO Exception is thrown
     */
    public void save(Path folder) throws SeMoDeException {
        try {
            Files.createDirectories(folder);
            saveCSV(folder);
            saveAdditionalInformation(folder);
        } catch (IOException e) {
            throw new SeMoDeException("Error saving profile to folder " + folder.toString(), e);
        }
    }

    private void saveCSV(Path folder) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(getHeader());         // header
        lines.add(getEmptyMetrics());   // adds empty metrics with (<start time>,0,0,..)
        for (ContainerMetrics metric : metrics) {
            lines.add(metric.formatCSVLine());
        }
        Files.write(folder.resolve("metrics.csv"), lines);
    }

    private void saveAdditionalInformation(Path folder) throws IOException, SeMoDeException {
        Gson jsonParser = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Files.write(folder.resolve("meta.json"), jsonParser.toJson(metaInfo).getBytes());
    }

    private String getHeader() {
        List<String> headerEntries = new ArrayList<>();
        headerEntries.add("timeStamp");
        headerEntries.addAll(metrics.get(0).relevantMetrics);
        return String.join(",", headerEntries);
    }

    private String getEmptyMetrics() {
        return additional.getState().getStartedAt() + StringUtils.repeat(",0", metrics.get(0).relevantMetrics.size());
    }

    private void validateMetrics(List<ContainerMetrics> metrics) throws SeMoDeException {
        if (metrics == null || metrics.isEmpty()) {
            throw new SeMoDeException("Metrics must be a non empty List.");
        }
    }

    private void validateAdditionalInformation(InspectContainerResponse additional) throws SeMoDeException {
        int exitCode = Optional
                .ofNullable(additional)
                .map(InspectContainerResponse::getState)
                .map(InspectContainerResponse.ContainerState::getExitCode)
                .orElseThrow(() -> new SeMoDeException("Exit code not present."));
        if (exitCode != 0 || !"exited".equals(additional.getState().getStatus())) {
            String exit = "exit code: " + exitCode;
            String status = "status: " + additional.getState().getStatus();
            throw new SeMoDeException(exit + " - " + status);
        }
    }

}
