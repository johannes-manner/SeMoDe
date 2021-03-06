package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class RunningCalibrationConfig {

    private String functionDockerSourceFolder;
    private String environmentVariablesFile;
    private int numberOfProfiles;

}
