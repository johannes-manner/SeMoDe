package de.uniba.dsg.serverless.pipeline.model.config;

import de.uniba.dsg.serverless.pipeline.calibration.local.LocalCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import lombok.Data;

import javax.persistence.*;

/**
 * Model class for calibration config and json serialization. DO NOT change this class. Otherwise json serialization and
 * deserialization does not work properly.
 */
@Data
@Entity
public class CalibrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // local parameter
    @Embedded
    private LocalCalibrationConfig localConfig;
    // aws parameter
    @OneToOne(cascade = CascadeType.ALL)
    private AWSCalibrationConfig awsCalibrationConfig;

    // for mappping
    @Embedded
    private MappingCalibrationConfig mappingCalibrationConfig;

    // for executing
    @Embedded
    private RunningCalibrationConfig runningCalibrationConfig;

    public CalibrationConfig() {
        this.localConfig = new LocalCalibrationConfig();
        this.awsCalibrationConfig = new AWSCalibrationConfig();
        this.mappingCalibrationConfig = new MappingCalibrationConfig();
        this.runningCalibrationConfig = new RunningCalibrationConfig();
    }
}
