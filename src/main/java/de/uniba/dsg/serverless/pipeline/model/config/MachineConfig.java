package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class MachineConfig {

    private String machineName;
    private String cpuModelName;
    private String modelNr;
}
