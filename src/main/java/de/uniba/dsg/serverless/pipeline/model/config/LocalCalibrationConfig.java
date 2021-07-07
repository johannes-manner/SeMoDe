package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class LocalCalibrationConfig {

    private double localSteps;
    private int numberOfLocalCalibrations;
    private String calibrationDockerSourceFolder;

    public LocalCalibrationConfig() {
    }
}
