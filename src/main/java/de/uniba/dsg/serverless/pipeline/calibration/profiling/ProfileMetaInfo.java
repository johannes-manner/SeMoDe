package de.uniba.dsg.serverless.pipeline.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

public class ProfileMetaInfo {

    public final String created;
    public final HostConfig hostConfig;
    public final String imageId;
    public final InspectContainerResponse.ContainerState state;
    public final long durationMS;
    public final long averageMemoryUsage;

    public ProfileMetaInfo(final Profile profile) throws SeMoDeException {
        final InspectContainerResponse additional = profile.additional;
        this.created = additional.getCreated();
        this.hostConfig = additional.getHostConfig();
        this.imageId = additional.getImageId();
        this.state = additional.getState();
        this.durationMS = ContainerMetrics.timeDifference(this.state.getStartedAt(), this.state.getFinishedAt());
        this.averageMemoryUsage = this.getAverageMemoryUtilization(profile);
    }


    private long getAverageMemoryUtilization(final Profile profile) throws SeMoDeException {
        long memoryUsage = 0;
        for (final ContainerMetrics entry : profile.metrics) {
            memoryUsage += entry.getMetric("memory_usage");
        }
        return memoryUsage / profile.metrics.size();
    }
}
