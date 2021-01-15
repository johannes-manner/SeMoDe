package de.uniba.dsg.serverless.pipeline.model.config;

import de.uniba.dsg.serverless.calibration.local.LocalCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import lombok.Data;

/**
 * Model class for calibration config and json serialization. DO NOT change this class. Otherwise json serialization and
 * deserialization does not work properly.
 */
@Data
public class CalibrationConfig {

    // local parameter
    private LocalCalibrationConfig localConfig;
    // aws parameter
    private AWSCalibrationConfig awsCalibrationConfig;

    // for mappping
    private MappingCalibrationConfig mappingCalibrationConfig;

    // for executing
    private RunningCalibrationConfig runningCalibrationConfig;

    public CalibrationConfig() {
        this.localConfig = new LocalCalibrationConfig();
        this.awsCalibrationConfig = new AWSCalibrationConfig();
        this.mappingCalibrationConfig = new MappingCalibrationConfig();
        this.runningCalibrationConfig = new RunningCalibrationConfig();
    }
}
