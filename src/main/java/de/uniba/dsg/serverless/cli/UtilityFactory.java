package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.cli.performance.AzurePerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.GooglePerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.IBMOpenWhiskPerformanceDataUtility;
import de.uniba.dsg.serverless.simulation.load.SimulationUtility;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UtilityFactory {

    private static final List<CustomUtility> utilityList = Arrays.asList(

            // fetcher for performance data
            new AzurePerformanceDataUtility("azurePerformanceData"),
            new IBMOpenWhiskPerformanceDataUtility("openWhiskPerformanceData"),
            new GooglePerformanceDataUtility("googlePerformanceData"),

            // parts of benchmarking
            new PipelineSetupUtility("pipelineSetup"),
            new DeploymentSizeUtility("deploymentSize"),

            // simulation
            new SimulationUtility("loadSimulation"),

            // calibration
            new CalibrationUtility("calibration")
    );

    public static Optional<CustomUtility> getUtilityClass(final String name) {
        return utilityList.stream().filter(c -> c.getName().equals(name)).findFirst();
    }
}
