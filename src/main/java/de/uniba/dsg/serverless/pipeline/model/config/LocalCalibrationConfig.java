package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;

@Data
@Embeddable
public class LocalCalibrationConfig {

    @OneToOne(cascade = CascadeType.ALL)
    private MachineConfig localMachineConfig;
    private double localSteps;
    private int numberOfLocalCalibrations;
    private boolean localEnabled;
    private String calibrationDockerSourceFolder;

    public LocalCalibrationConfig() {
    }
}
