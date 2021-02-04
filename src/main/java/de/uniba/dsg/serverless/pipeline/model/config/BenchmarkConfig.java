package de.uniba.dsg.serverless.pipeline.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Model class for benchmark execution config and json serialization. DO NOT change this class. Otherwise json
 * serialization and deserialization does not work properly.
 */
// TODO add bean validation to the class (not completed yet)
@Data
@Entity
@NoArgsConstructor
public class BenchmarkConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    //    @NotNull(message = "Select one of the benchmark modes")
    private boolean deployed = false;
    private String benchmarkMode;
    private String benchmarkParameters;
    private String postArgument;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int versionNumber;
    // attribute for easy finding N benchmark configs to a single setup no need for many to one and faster retrieval of
    // the actual benchmark config in the setup config
    private String setupName;
    @JsonIgnore
    @OneToOne(mappedBy = "benchmarkConfig")
    private SetupConfig setupConfig;

    // aws parameters
    @Embedded
    private AWSBenchmarkConfig awsBenchmarkConfig;

    public BenchmarkConfig(SetupConfig setupConfig) {
        this.awsBenchmarkConfig = new AWSBenchmarkConfig();
        this.setupConfig = setupConfig;
        this.setupName = setupConfig.getSetupName();
    }


    /**
     * Logs the start time, when the benchmark is started. Value is needed for a later retrieval of the information from
     * the corresponding platform.
     */
    public void logBenchmarkStartTime() {
        this.startTime = LocalDateTime.now();
    }

    /**
     * Logs the end time, when the benchmark is finished and the last execution terminated. Value is needed for a later
     * retrieval of the information from the corresponding platform.
     */
    public void logBenchmarkEndTime() {
        this.endTime = LocalDateTime.now();
    }
}
