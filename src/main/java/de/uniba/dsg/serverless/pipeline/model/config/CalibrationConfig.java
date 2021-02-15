package de.uniba.dsg.serverless.pipeline.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * Model class for calibration config and json serialization. DO NOT change this class. Otherwise json serialization and
 * deserialization does not work properly.
 */
@Data
@Entity
@NoArgsConstructor
// TODO check if needed...
@NamedEntityGraph(name = "CalibrationConfig.calibrationEvents",
        attributeNodes = @NamedAttributeNode(value = "calibrationEvents"))
public class CalibrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // attribute for easy finding N calibration configs to a single setup no need for many to one and faster retrieval of
    // the actual benchmark config in the setup config
    private boolean deployed;
    private int versionNumber;
    private String setupName;

    // local parameter
    @Embedded
    private LocalCalibrationConfig localConfig;
    // aws parameter
    @Embedded
    private AWSCalibrationConfig awsCalibrationConfig;

    // for mappping
    @Embedded
    private MappingCalibrationConfig mappingCalibrationConfig;

    // for executing
    @Embedded
    private RunningCalibrationConfig runningCalibrationConfig;

    @JsonIgnore
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "config")
    private List<CalibrationEvent> calibrationEvents;

    public CalibrationConfig(SetupConfig config) {
        this.localConfig = new LocalCalibrationConfig();
        this.awsCalibrationConfig = new AWSCalibrationConfig();
        this.mappingCalibrationConfig = new MappingCalibrationConfig();
        this.runningCalibrationConfig = new RunningCalibrationConfig();
        this.setupName = config.getSetupName();
    }

    public void increaseVersion() {
        this.id = null;
        this.versionNumber++;
    }

    // TODO fix it - recursion problem
    @Override
    public String toString() {
        return "";
    }
}
