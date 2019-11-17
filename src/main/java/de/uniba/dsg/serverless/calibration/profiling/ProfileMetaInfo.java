package de.uniba.dsg.serverless.calibration.profiling;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class ProfileMetaInfo {

    public final String created;
    public final HostConfig hostConfig;
    public final String imageId;
    public final InspectContainerResponse.ContainerState state;
    public final long durationMS;

    public ProfileMetaInfo(Profile profile) throws SeMoDeException {
        InspectContainerResponse additional = profile.additional;
        created = additional.getCreated();
        hostConfig = additional.getHostConfig();
        imageId = additional.getImageId();
        state = additional.getState();
        durationMS = ContainerMetrics.timeDifference(state.getStartedAt(), state.getFinishedAt());
    }
}
