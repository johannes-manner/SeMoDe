package de.uniba.dsg.serverless.pipeline.model.config.aws;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class AWSCalibrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // AWS specific information
    private String bucketName;
    private int numberOfAWSExecutions;

    @Embedded
    private AWSBenchmarkConfig benchmarkConfig;

    public AWSCalibrationConfig() {
        this.benchmarkConfig = new AWSBenchmarkConfig();
    }

    /**
     * Resets the system generated values and the identifier and names from the aws cloud platform. Leaves the
     * <i>settings.json</i> in a consistent state. <br/> If you alter this method, also check {@link
     * AWSBenchmarkConfig#resetConfig()}.
     */
    public void resetConfig() {
        this.benchmarkConfig.resetConfig();
    }
}


