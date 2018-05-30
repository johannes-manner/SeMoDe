package de.uniba.dsg.serverless.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.uniba.dsg.serverless.cli.performance.AWSPerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.AzurePerformanceDataUtility;
import de.uniba.dsg.serverless.cli.performance.GooglePerformanceDataUtility;

public class UtilityFactory {

	private static List<CustomUtility> utilityList = Arrays.asList(
			new AWSPerformanceDataUtility("awsPerformanceData"),
			new AzurePerformanceDataUtility("azurePerformanceData"),
			new GooglePerformanceDataUtility("googlePerformanceData"),
			new SeMoDeUtility("awsSeMoDe"),
			new BenchmarkUtility("benchmark"),
			new DeploymentSizeUtility("deploymentSize")
	);

	public static Optional<CustomUtility> getUtilityClass(String name) {
		return utilityList.stream().filter(c -> c.getName().equals(name)).findFirst();
	}
}
