package de.uniba.dsg.serverless.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UtilityFactory {

	private static List<CustomUtility> utilityList = Arrays.asList(
			new PerformanceDataUtility("awsPerformanceData"),
			new SeMoDeUtility("awsSeMoDe")
	);

	public static Optional<CustomUtility> getUtilityClass(String name) {
		return utilityList.stream().filter(c -> c.getName().equals(name)).findFirst();
	}
}
