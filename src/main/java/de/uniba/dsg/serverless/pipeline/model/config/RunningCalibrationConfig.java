package de.uniba.dsg.serverless.pipeline.model.config;

import com.google.common.primitives.Ints;
import com.google.gson.annotations.Expose;

public class RunningCalibrationConfig {

    @Expose
    public String dockerSourceFolder;
    @Expose
    public String environmentVariablesFile;
    @Expose
    public int numberOfProfiles;

    public RunningCalibrationConfig() {

    }

    public RunningCalibrationConfig(final RunningCalibrationConfig other) {
        this.dockerSourceFolder = other.dockerSourceFolder;
        this.environmentVariablesFile = other.environmentVariablesFile;
        this.numberOfProfiles = other.numberOfProfiles;
    }

    @Override
    public String toString() {
        return "RunningCalibrationConfig{" +
                "dockerSourceFolder='" + this.dockerSourceFolder + '\'' +
                ", environmentVariablesFile='" + this.environmentVariablesFile + '\'' +
                ", numberOfProfiles=" + this.numberOfProfiles +
                '}';
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
