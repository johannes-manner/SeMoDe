package de.uniba.dsg.serverless.benchmark.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FetchingCommandGenerator {

	public static void main(String[] args) {
		// GENERAL
		String path = "C:/Users/jmanner/Desktop";
		Path pathToEndpoints = Paths.get(path + "/Test Setup/endpoints/azure-fibonacci-java.txt");
		Path benchmarkLogsFolder = Paths.get(path + "/Test Setup/benchmarking-logs/azure-java");
		String logPrefix = "logs/";
		String startTime = "2018-01-01_00:00";
		String endTime = "2018-12-01_00:00";
		String outputCommands = path + "/Test Setup/fetch-commands/azure-java";
		FetcherType type = FetcherType.AZURE_MAVEN;

		// AWS specific
		// "eu-west-1" for dublin ( js )
		// "eu-west-4" for paris ( setup 5 )
		// "eu-central-1" for frankfurt ( java )
		String region = "eu-central-1";

		// Azure specific
		Path apiKeysFolder = Paths.get(path + "/Test Setup/insights-keys/azure-java/");
		String functionName = "fibonacci-java";

		try {
			List<String> functionNames = getFunctionNames(pathToEndpoints);

			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputCommands), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {

				List<String> commands = null;
				switch (type) {
				case AWS:
					commands = getAWSFetchCommands(functionNames, benchmarkLogsFolder, logPrefix, region, startTime,
							endTime);
					break;
				case AZURE_MAVEN:
					commands = getAzureFetchCommands(functionNames, benchmarkLogsFolder, apiKeysFolder, logPrefix,
							Optional.of(functionName), startTime, endTime);
					break;
				case AZURE_SERVERLESS:
					commands = getAzureFetchCommands(functionNames, benchmarkLogsFolder, apiKeysFolder, logPrefix,
							Optional.empty(), startTime, endTime);
					break;
				}
				for (String command : commands) {
					writer.write(command);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			System.err.println("Something went wrong. " + e.getMessage()
					+ (e.getCause() != null ? e.getCause().getMessage() : ""));
			System.err.println("terminating...");
			e.printStackTrace();
			return;
		}
		System.out.println("Success");
	}

	private static List<String> getFunctionNames(Path pathToEndpoints) throws IOException {
		List<String> functionNames = new ArrayList<>();
		for (String functionPlusUrl : Files.readAllLines(pathToEndpoints)) {
			functionNames.add(functionPlusUrl.split(" ")[0]);
		}
		return functionNames;

	}

	private static List<String> getAWSFetchCommands(List<String> functionNames, Path benchmarkLogsFolder,
			String logPrefix, String region, String startTime, String endTime) throws IOException {
		List<String> commands = new ArrayList<>();
		Map<String, String> logFilesMap = getBenchmarkLogsMap(benchmarkLogsFolder);

		for (String functionName : functionNames) {
			String logGroup = "/aws/lambda/" + functionName + "-dev-" + functionName;
			String logFile = logFilesMap.get(functionName);
			Objects.requireNonNull(logFile, "LogFile does not exist. There exists a mismatch in the input files.");
			logFile = "\""+logPrefix + logFile + "\"";

			// "start cmd /C " would be the parallel version which has issues unfortunately.
			// Performance data is missing when executing in parallel
			commands.add("java -jar build/libs/SeMoDe.jar awsPerformanceData " + region + " " + logGroup + " "
					+ startTime + " " + endTime + " " + logFile);
		}

		return commands;
	}

	private static List<String> getAzureFetchCommands(List<String> serviceNames, Path benchmarkLogsFolder,
			Path apiKeysFolder, String logPrefix, Optional<String> functionName, String startTime, String endTime)
			throws IOException {
		List<String> commands = new ArrayList<>();
		Map<String, String> logFilesMap = getBenchmarkLogsMap(benchmarkLogsFolder);
		Map<String, String[]> apiKeysMap = getApiKeysMap(apiKeysFolder);
		if (apiKeysMap.size() != serviceNames.size()) {
			throw new IllegalArgumentException("number of function names (" + serviceNames.size()
					+ ") must be the same length as number of api keys (" + apiKeysMap.size() + ")");
		}
		for (String serviceName : serviceNames) {
			String[] apiKey = apiKeysMap.get(serviceName);
			Objects.requireNonNull(apiKey, "Api key does not exists. Wrong folder entered");

			String logFile = logFilesMap.get(serviceName);
			Objects.requireNonNull(logFile, "LogFile does not exist. There exists a mismatch in the input files.");
			logFile = logPrefix + logFile;

			if (functionName.isPresent()) {
				commands.add(
						"start cmd /C java -jar build/libs/SeMoDe.jar azurePerformanceData " + apiKey[0] + " " + apiKey[1]
								+ " " + serviceName + " " + functionName.get() + " " + startTime + " " + endTime + " " + logFile);
			}
			else {
				commands.add(
						"start cmd /C java -jar build/libs/SeMoDe.jar azurePerformanceData " + apiKey[0] + " " + apiKey[1]
								+ " " + serviceName + " " + serviceName + " " + startTime + " " + endTime + " " + logFile);
			}
		}
		return commands;
	}

	private static Map<String, String[]> getApiKeysMap(Path apiKeysFolder) throws IOException {
		Map<String, String[]> map = new HashMap<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(apiKeysFolder)) {
			for (Path entry : stream) {
				String fileName = entry.getFileName().toString();
				String[] apiKey = new String[2];
				apiKey = Files.readAllLines(entry).toArray(apiKey);
				map.put(fileName, apiKey);
			}
		}

		return map;
	}

	/**
	 * Returns a Map where the functionName maps to the Name of the logfile
	 * 
	 * @param logsFolder
	 *            Must be an existing folder with *.log files
	 * @return Map
	 * @throws IOException
	 */
	private static Map<String, String> getBenchmarkLogsMap(Path logsFolder) throws IOException {
		int prefixLength = "benchmarking_MM-dd-HH-mm-ss_".length();
		Map<String, String> map = new HashMap<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsFolder, "*.log")) {
			for (Path entry : stream) {
				String fileName = entry.getFileName().toString();
				String functionName = fileName.substring(prefixLength, fileName.length() - ".log".length());
				map.put(functionName, fileName);
			}
		}

		return map;
	}

}
