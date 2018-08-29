package de.uniba.dsg.serverless.pipeline.utils;

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
import java.util.Scanner;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.LanguageConfig;

/**
 * Settings for generating the fetching commands are very provider specific.
 * Therefore each provider is treated separately in this class.
 *
 */
public class FetchingCommandGenerator {
	
	private static final Scanner scanner = new Scanner(System.in);
	private final Path pathToBenchmarkLogs;
	private final Path pathToFetchingCommands;
	private final Path pathToEndpoints;
	private final Map<String, LanguageConfig> languageConfig;
	
	public FetchingCommandGenerator(Path pathToBenchmarkFolder, Path pathToFetchingCommands, Path pathToEndpoints, Map<String, LanguageConfig> languageConfig) {
		this.pathToBenchmarkLogs = Paths.get(pathToBenchmarkFolder.toString(), "logs");
		this.pathToFetchingCommands = pathToFetchingCommands;
		this.pathToEndpoints = pathToEndpoints;
		this.languageConfig = languageConfig;
	}
	
	
	public void fetchCommands(String provider, String language) throws SeMoDeException {

		// provider independent parameters
		System.out.println("Specify a start time in the format YYYY-MM-DD_HH:MM");
		String startTime = scanner.nextLine();
		System.out.println("Specify a end time in the format YYYY-MM-DD_HH:MM");
		String endTime = scanner.nextLine();
		
		// provider specific parameters
		if("aws".equals(provider)) {
			System.out.println("Specify the region, where the functions were executed");
			String region = scanner.nextLine();
			
			this.generateCommands(provider, language, startTime, endTime, region);
		}else if("azure".equals(provider)){
			System.out.println("Specify the application insights key folder");
			String insightsFolder = scanner.nextLine();
			System.out.println("Specify the function name");
			String functionName = scanner.nextLine();
			if(functionName == null) {
				functionName = "";
			}
			
			this.generateCommands(provider, language, startTime, endTime, insightsFolder, functionName);
		} else if ("google".equals(provider)) {
			this.generateCommands(provider, language, startTime, endTime);
		} else {
			throw new SeMoDeException("Provider is not supported " + provider);
		}
	}

	private void generateCommands(String provider, String language, String startTime, String endTime, String... args) throws SeMoDeException {
		// GENERAL
		String providerLanguage = provider + "-" + language;
		String type = this.languageConfig.get(providerLanguage).getFetcherType();

		try {
			List<String> functionNames = getFunctionNames(providerLanguage);

			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.pathToFetchingCommands.toString(), providerLanguage + ".bat"), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {

				List<String> commands = null;
				Map<String, String> logFilesMap = this.getBenchmarkLogsMap(provider);
				switch (type) {
				case "aws":
					commands = getAWSFetchCommands(functionNames, this.pathToBenchmarkLogs, logFilesMap, providerLanguage, args[0], startTime,
							endTime);
					break;
				case "azure_maven":
					commands = getAzureFetchCommands(functionNames, this.pathToBenchmarkLogs, Paths.get(args[0]), logFilesMap, providerLanguage, 
							Optional.of(args[1]), startTime, endTime);
					break;
				case "azure_serverless":
					commands = getAzureFetchCommands(functionNames, this.pathToBenchmarkLogs, Paths.get(args[0]), logFilesMap, providerLanguage, 
							Optional.empty(), startTime, endTime);
					break;
				case "google":
					commands = getGoogleFetchCommands(functionNames, logFilesMap, startTime, endTime);
					break;
				}
				for (String command : commands) {
					writer.write(command);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Error while writing and reading files.", e);
		}
		System.out.println("Success");
	}

	/**
	 * Path to endpoints is easier accessible because the function names are the first part of each line.
	 * @throws IOException
	 */
	private List<String> getFunctionNames(String fileName) throws IOException {
		
		List<String> functionNames = new ArrayList<>();
		for (String functionPlusUrl : Files.readAllLines(Paths.get(this.pathToEndpoints.toString(), fileName))) {
			functionNames.add(functionPlusUrl.split(" ")[0]);
		}
		return functionNames;
	}

	private List<String> getAWSFetchCommands(List<String> functionNames, Path benchmarkLogsFolder, Map<String, String> logFilesMap,
			String logPrefix, String region, String startTime, String endTime) throws IOException {
		List<String> commands = new ArrayList<>();
		
		for (String functionName : functionNames) {
			String logGroup = "/aws/lambda/" + functionName + "-dev-" + functionName;
			String logFile = logFilesMap.get(functionName);
			Objects.requireNonNull(logFile, "LogFile does not exist. There exists a mismatch in the input files.");
			
			// "start cmd /C " would be the parallel version which has issues unfortunately.
			// Performance data is missing when executing in parallel
			// hard coded relative paths, because also the folder structure is created in this prototype
			// there should be no change due to the folder structure
			commands.add("java -jar ../../../build/libs/SeMoDe.jar awsPerformanceData " + region + " " + logGroup + " "
					+ startTime + " " + endTime + " " + "../benchmarkingCommands/logs/" + logFile);
		}

		return commands;
	}
	
	private List<String> getGoogleFetchCommands(List<String> functionNames,
			Map<String, String> logFilesMap, String startTime, String endTime) {

		List<String> commands = new ArrayList<>();
		
		for(String function : functionNames) {
			String logFile = logFilesMap.get(function);
			
			commands.add("start cmd /C java -jar ../../../build/libs/SeMoDe.jar googlePerformanceData " + function + " " + startTime + " " + endTime + " " +  "../benchmarkingCommands/logs/" + logFile);
		}
		
		return commands;
	}

	private List<String> getAzureFetchCommands(List<String> serviceNames, Path benchmarkLogsFolder,
			Path apiKeysFolder, Map<String, String> logFilesMap, String logPrefix, Optional<String> functionName, String startTime, String endTime)
			throws IOException {
		List<String> commands = new ArrayList<>();
		Map<String, String[]> apiKeysMap = this.getApiKeysMap(apiKeysFolder);
		if (apiKeysMap.size() != serviceNames.size()) {
			throw new IllegalArgumentException("number of function names (" + serviceNames.size()
					+ ") must be the same length as number of api keys (" + apiKeysMap.size() + ")");
		}
		for (String serviceName : serviceNames) {
			String[] apiKey = apiKeysMap.get(serviceName);
			Objects.requireNonNull(apiKey, "Api key does not exists. Wrong folder entered");

			String logFile = logFilesMap.get(serviceName);
			Objects.requireNonNull(logFile, "LogFile does not exist. There exists a mismatch in the input files.");
			
			// hard coded relative paths, because also the folder structure is created in this prototype
			// there should be no change due to the folder structure
			if (functionName.isPresent()) {
				commands.add("start cmd /C java -jar ../../../build/libs/SeMoDe.jar azurePerformanceData " + apiKey[0] + " "
						+ apiKey[1] + " " + serviceName + " " + functionName.get() + " " + startTime + " " + endTime
						+ " " + "../benchmarkingCommands/logs/" + logFile);
			} else {
				commands.add("start cmd /C java -jar ../../../build/libs/SeMoDe.jar azurePerformanceData " + apiKey[0] + " "
						+ apiKey[1] + " " + serviceName + " " + serviceName + " " + startTime + " " + endTime + " "
						+ "../benchmarkingCommands/logs/" + logFile);
			}
		}
		return commands;
	}

	private Map<String, String[]> getApiKeysMap(Path apiKeysFolder) throws IOException {
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
	private Map<String, String> getBenchmarkLogsMap(String provider) throws IOException {
		int prefixLength = "benchmarking_MM-dd-HH-mm-ss_".length() + provider.length() + "_".length();
		Map<String, String> map = new HashMap<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.pathToBenchmarkLogs, "*.log")) {
			for (Path entry : stream) {
				String fileName = entry.getFileName().toString();
				String functionName = fileName.substring(prefixLength, fileName.length() - ".log".length());
				map.put(functionName, fileName);
			}
		}

		return map;
	}
}
