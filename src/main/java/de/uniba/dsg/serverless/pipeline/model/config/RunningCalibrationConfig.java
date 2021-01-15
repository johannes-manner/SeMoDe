package de.uniba.dsg.serverless.pipeline.model.config;

import com.google.common.primitives.Ints;
import com.google.gson.annotations.Expose;
import lombok.Data;

// TODO validation
@Data
public class RunningCalibrationConfig {

    @Expose
    // TODO image??
    public String dockerSourceFolder;
    @Expose
    public String environmentVariablesFile;
    @Expose
    public int numberOfProfiles;

    public RunningCalibrationConfig() {

    }

    public void update(final String dockerSourceFolder, final String environmentVariablesFile, final String numberOfProfiles) {
        if (!"".equals(dockerSourceFolder)) {
            this.dockerSourceFolder = dockerSourceFolder;
        }
        if (!"".equals(environmentVariablesFile)) {
            this.environmentVariablesFile = environmentVariablesFile;
        }
        if (!"".equals(numberOfProfiles) && Ints.tryParse(numberOfProfiles) != null) {
            this.numberOfProfiles = Ints.tryParse(numberOfProfiles);
        }
    }
}
