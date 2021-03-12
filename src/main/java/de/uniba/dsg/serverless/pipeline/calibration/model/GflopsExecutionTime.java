package de.uniba.dsg.serverless.pipeline.calibration.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data holder class to return gflops and the execution time.
 */
@Data
@AllArgsConstructor
public class GflopsExecutionTime {
    private double gflops;
    private double executionTimeInS;
}
