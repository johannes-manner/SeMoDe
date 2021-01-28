package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;
import lombok.NoArgsConstructor;

// TODO validation
@Data
@NoArgsConstructor
public class RunningCalibrationConfig {

    private String dockerSourceFolder;
    private String environmentVariablesFile;
    private int numberOfProfiles;

}
