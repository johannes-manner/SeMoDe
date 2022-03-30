package de.uniba.dsg.serverless.pipeline.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.openfaas.OpenFaasBenchmarkConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@NamedEntityGraph(name = "BenchmarkConfig.localExecutionEvents",
        attributeNodes = @NamedAttributeNode(value = "localExecutionEvents"))
public class BenchmarkConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    //    @NotNull(message = "Select one of the benchmark modes")
    private boolean deployed = false;
    private String description;
    private String benchmarkMode;
    private String benchmarkParameters;
    private String postArgument;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int versionNumber;
    // property to decide, if the version of the experiment is visible for all users which are
    // not authenticated to make the experiments publicly available
    private boolean versionVisible;
    // attribute for easy finding N benchmark configs to a single setup no need for many to one and faster retrieval of
    // the actual benchmark config in the setup config
    private String setupName;
    @JsonIgnore
    @OneToOne(mappedBy = "benchmarkConfig")
    private SetupConfig setupConfig;

    // aws parameters
    @Embedded
    private AWSBenchmarkConfig awsBenchmarkConfig;

    @Embedded
    private OpenFaasBenchmarkConfig openFaasBenchmarkConfig;

    @JsonIgnore
    @OneToMany(mappedBy = "benchmarkConfig", fetch = FetchType.LAZY)
    private List<LocalRESTEvent> localExecutionEvents;

    @Transient
    private int numberOfLocalDbEvents;
    @Transient
    private int numberOfFetchedData;

    public BenchmarkConfig(SetupConfig setupConfig) {
        this.awsBenchmarkConfig = new AWSBenchmarkConfig();
        this.openFaasBenchmarkConfig = new OpenFaasBenchmarkConfig();
        this.setupConfig = setupConfig;
        this.setupName = setupConfig.getSetupName();
    }

    // copy constructor
    public BenchmarkConfig(BenchmarkConfig benchmarkConfig) {
        this.id = benchmarkConfig.id;
        this.deployed = benchmarkConfig.deployed;
        this.description = benchmarkConfig.description;
        this.benchmarkMode = benchmarkConfig.benchmarkMode;
        this.benchmarkParameters = benchmarkConfig.benchmarkParameters;
        this.postArgument = benchmarkConfig.postArgument;
        this.startTime = benchmarkConfig.startTime;
        this.endTime = benchmarkConfig.endTime;
        this.versionNumber = benchmarkConfig.versionNumber;
        this.setupName = benchmarkConfig.setupName;
        this.setupConfig = benchmarkConfig.setupConfig;
        this.awsBenchmarkConfig = benchmarkConfig.awsBenchmarkConfig;
        this.openFaasBenchmarkConfig = benchmarkConfig.openFaasBenchmarkConfig;
        this.localExecutionEvents = benchmarkConfig.localExecutionEvents;
        this.numberOfLocalDbEvents = benchmarkConfig.numberOfLocalDbEvents;
        this.numberOfFetchedData = benchmarkConfig.numberOfFetchedData;
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

    public BenchmarkConfig increaseVersion() {
        BenchmarkConfig config = new BenchmarkConfig(this);
        config.id = null;
        config.versionNumber++;
        return config;
    }

    @Override
    public String toString() {
        return "BenchmarkConfig{" +
                "id=" + id +
                ", deployed=" + deployed +
                ", benchmarkMode='" + benchmarkMode + '\'' +
                ", benchmarkParameters='" + benchmarkParameters + '\'' +
                ", postArgument='" + postArgument + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", versionNumber=" + versionNumber +
                ", setupName='" + setupName + '\'' +
                ", setupConfig=" + setupConfig.getSetupName() +
                ", awsBenchmarkConfig=" + awsBenchmarkConfig +
                ", openFaasBenchmarkConfig=" + openFaasBenchmarkConfig +
                ", localExecutionEvents=" + localExecutionEvents.size() +
                ", numberOfLocalDbEvents=" + numberOfLocalDbEvents +
                ", numberOfFetchedData=" + numberOfFetchedData +
                '}';
    }
}
