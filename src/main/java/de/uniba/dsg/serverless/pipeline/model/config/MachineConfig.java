package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class MachineConfig {
    @Id
    private String identifier;
    private String cpuModelName;
    private String modelNr;
}
