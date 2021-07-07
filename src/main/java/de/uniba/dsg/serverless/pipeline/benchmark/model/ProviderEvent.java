package de.uniba.dsg.serverless.pipeline.benchmark.model;

import lombok.Data;

import javax.persistence.*;

/**
 * This class is a model class for the benchmark interaction to hold the metainformation responded by the cloud
 * function. It is important that your custom implementation of your cloud function when using our invocation
 * mechanism uses this interface and return the data in JSON format!!
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

    @OneToOne(cascade = CascadeType.ALL)
    private PerformanceData performanceData;
}
