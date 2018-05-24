package de.uniba.dsg.serverless.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UtilityFactory {

	private static List<CustomUtility> utilityList = Arrays.asList(
			new AWSPerformanceDataUtility("awsPerformanceData"),
			new AzurePerformanceDataUtility("azurePerformanceData"),
			new SeMoDeUtility("awsSeMoDe"),
			new BenchmarkUtility("benchmark"),
			new DeploymentSizeUtility("deploymentSize")
	);

	public static Optional<CustomUtility> getUtilityClass(String name) {
		return utilityList.stream().filter(c -> c.getName().equals(name)).findFirst();
	}
}
