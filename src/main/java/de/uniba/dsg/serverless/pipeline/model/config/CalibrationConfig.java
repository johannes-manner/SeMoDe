package de.uniba.dsg.serverless.pipeline.model.config;

import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Model class for calibration config and json serialization. DO NOT change this class. Otherwise json serialization and
 * deserialization does not work properly.
 */
@Data
@Entity
@NoArgsConstructor
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
}
