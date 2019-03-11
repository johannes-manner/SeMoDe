package de.uniba.dsg.serverless.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.uniba.dsg.serverless.cli.performance.AWSPerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.AzurePerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.GooglePerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.IBMOpenWhiskPerformanceDataUtility;
import de.uniba.dsg.serverless.simulation.load.SimulationUtility;

public class UtilityFactory {

	private static List<CustomUtility> utilityList = Arrays.asList(
			
			// fetcher for performance data
			new AWSPerformanceDataUtility("awsPerformanceData"),
			new AzurePerformanceDataUtility("azurePerformanceData"),
			new IBMOpenWhiskPerformanceDataUtility("openWhiskPerformanceData"),
			new GooglePerformanceDataUtility("googlePerformanceData"),
			
			// parts of benchmarking
			new PipelineSetupUtility("pipelineSetup"),
			new BenchmarkUtility("benchmark"),
			new DeploymentSizeUtility("deploymentSize"),
			
			// automated test generation
			new SeMoDeUtility("awsSeMoDe"),		
			
			// simulation
			new SimulationUtility("loadSimulation")
	);

	public static Optional<CustomUtility> getUtilityClass(String name) {
		return utilityList.stream().filter(c -> c.getName().equals(name)).findFirst();
	}
}
