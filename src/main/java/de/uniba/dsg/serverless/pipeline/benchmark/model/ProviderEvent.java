package de.uniba.dsg.serverless.pipeline.benchmark.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

// TODO comment properly!! These information has to be returned by the user which writes the cloud functions!

/**
 * This class is a model class for the benchmark interaction to hold the metainformation responded by the cloud
 * function.
 * <p>
 * Metainformation include the platform ID, the container ID and the information about the execution environment/VM
 * configuration, if possible.
 */
@Data
@Entity
public class ProviderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String result;
    private String platformId;
    private String containerId;
    private String vmIdentification;
    private String cpuModel;
    private String cpuModelName;
}
