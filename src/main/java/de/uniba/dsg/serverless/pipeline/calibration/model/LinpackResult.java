package de.uniba.dsg.serverless.pipeline.calibration.model;

import de.uniba.dsg.serverless.pipeline.model.config.MachineConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data holder class to return gflops and the execution time.
 */
@Data
@AllArgsConstructor
public class LinpackResult {
    private double gflops;
    private double executionTimeInS;
    private MachineConfig machineConfig;
}
