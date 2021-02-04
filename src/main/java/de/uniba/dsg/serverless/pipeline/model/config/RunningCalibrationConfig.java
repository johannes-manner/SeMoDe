package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;

// TODO validation
@Data
@Embeddable
public class RunningCalibrationConfig {

    @OneToOne(cascade = CascadeType.ALL)
    private MachineConfig runningMachineConfig;
    private String functionDockerSourceFolder;
    private String environmentVariablesFile;
    private int numberOfProfiles;

}
