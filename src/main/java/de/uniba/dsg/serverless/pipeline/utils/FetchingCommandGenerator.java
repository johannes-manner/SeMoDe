package de.uniba.dsg.serverless.pipeline.utils;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.LanguageConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Settings for generating the fetching commands are very provider specific.
 * Therefore each provider is treated separately in this class.
 *
 * @deprecated since rewriting of the pipeline in 2020
 */
@Deprecated
public class FetchingCommandGenerator {

    private static final Scanner scanner = new Scanner(System.in);
    private final Path pathToBenchmarkLogs;
    private final Path pathToFetchingCommands;
    private final Path pathToEndpoints;
    private final Map<String, LanguageConfig> languageConfig;
    private final String startTime;
    private final String endTime;
    private final String seMoDeJarLocation;

    @Deprecated
    public FetchingCommandGenerator(final Path pathToBenchmarkFolder, final Path pathToFetchingCommands, final Path pathToEndpoints, final Map<String, LanguageConfig> languageConfig, final String seMoDeJarLocation) {
        this.pathToBenchmarkLogs = Paths.get(pathToBenchmarkFolder.toString(), "logs");
        this.pathToFetchingCommands = pathToFetchingCommands;
        this.pathToEndpoints = pathToEndpoints;
        this.languageConfig = languageConfig;
        this.seMoDeJarLocation = seMoDeJarLocation;

        // provider independent parameters
        System.out.println("Specify a start time in the format YYYY-MM-DD_HH:MM");
        this.startTime = scanner.nextLine();
        System.out.println("Specify a end time in the format YYYY-MM-DD_HH:MM");
        this.endTime = scanner.nextLine();
    }

    @Deprecated
    public void fetchCommands(final String provider, final String language) throws SeMoDeException {

        final String providerLanguage = provider + "-" + language;

        // provider specific parameters
        if ("aws".equals(provider)) {
            System.out.println("(" + providerLanguage + ") Specify the region, where the functions were executed");
            final String region = scanner.nextLine();

            this.generateCommands(provider, language, this.startTime, this.endTime, region);
        } else if ("azure".equals(provider)) {
            System.out.println("(" + providerLanguage + ") Specify the application insights key folder");
            final String insightsFolder = scanner.nextLine();
            System.out.println("(" + providerLanguage + ") Specify the function name");
            String functionName = scanner.nextLine();
            if (functionName == null) {
                functionName = "";
            }

            this.generateCommands(provider, language, this.startTime, this.endTime, insightsFolder, functionName);
        } else if ("google".equals(provider)) {
            this.generateCommands(provider, language, this.startTime, this.endTime);
        } else if ("ibm".equals(provider)) {
            System.out.println("(" + providerLanguage + ") Specify the authorization key to get log infos from ibm openwhisk");
            final String authorizationKey = scanner.nextLine();
            System.out.println("(" + providerLanguage + ") Specify the namespace (defaultOrg_defaultSpace)");
            final String namespace = scanner.nextLine();

            this.generateCommands(provider, language, this.startTime, this.endTime, authorizationKey, namespace);
        } else {
            throw new SeMoDeException("Provider is not supported " + provider);
        }
    }

    private void generateCommands(final String provider, final String language, final String startTime, final String endTime, final String... args) throws SeMoDeException {
        // GENERAL
        final String providerLanguage = provider + "-" + language;
        final String type = this.languageConfig.get(providerLanguage).getFetcherType();

        try {
            final List<String> functionNames = this.getFunctionNames(providerLanguage);

            try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.pathToFetchingCommands.toString(), providerLanguage + ".bat"), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {

                List<String> commands = null;
                final Map<String, String> logFilesMap = this.getBenchmarkLogsMap(provider);
                switch (type) {
                    case "aws":
                        commands = this.getAWSFetchCommands(functionNames, this.pathToBenchmarkLogs, logFilesMap, providerLanguage, args[0], startTime,
                                endTime);
                        break;
                    case "azure_maven":
                        commands = this.getAzureFetchCommands(functionNames, this.pathToBenchmarkLogs, Paths.get(args[0]), logFilesMap, providerLanguage,
                                Optional.of(args[1]), startTime, endTime);
                        break;
                    case "azure_serverless":
                        commands = this.getAzureFetchCommands(functionNames, this.pathToBenchmarkLogs, Paths.get(args[0]), logFilesMap, providerLanguage,
                                Optional.empty(), startTime, endTime);
                        break;
                    case "google":
                        commands = this.getGoogleFetchCommands(functionNames, logFilesMap, startTime, endTime);
                        break;
                    case "ibm":
                        commands = this.getIbmFetchCommands(functionNames, logFilesMap, startTime, endTime, args[0], args[1]);
                        break;
                }
                for (final String command : commands) {
                    writer.write(command);
                    writer.newLine();
                }
            }
        } catch (final IOException e) {
            throw new SeMoDeException("Error while writing and reading files.", e);
        }
        System.out.println("Success");
    }

    /**
     * Path to endpoints is easier accessible because the function names are the first part of each line.
     *
     * @throws IOException
     */
    private List<String> getFunctionNames(final String fileName) throws IOException {

        final List<String> functionNames = new ArrayList<>();
        for (final String functionPlusUrl : Files.readAllLines(Paths.get(this.pathToEndpoints.toString(), fileName))) {
            functionNames.add(functionPlusUrl.split(" ")[0]);
        }
        return functionNames;
    }

    private List<String> getAWSFetchCommands(final List<String> functionNames, final Path benchmarkLogsFolder, final Map<String, String> logFilesMap,
                                             final String logPrefix, final String region, final String startTime, final String endTime) throws IOException {
        final List<String> commands = new ArrayList<>();

        for (final String functionName : functionNames) {
            final String logGroup = "/aws/lambda/" + functionName + "-dev-" + functionName;
            final String logFile = logFilesMap.get(functionName);
            Objects.requireNonNull(logFile, "LogFile does not exist. There exists a mismatch in the input files.");

            // "start cmd /C " would be the parallel version which has issues unfortunately.
            // Performance data is missing when executing in parallel
            // hard coded relative paths, because also the folder structure is created in this prototype
            // there should be no change due to the folder structure
            commands.add("java -jar " + this.seMoDeJarLocation + " awsPerformanceData " + region + " " + logGroup + " "
                    + startTime + " " + endTime + " " + "../benchmarkingCommands/logs/" + logFile);
        }

        return commands;
    }

    private List<String> getGoogleFetchCommands(final List<String> functionNames,
                                                final Map<String, String> logFilesMap, final String startTime, final String endTime) {

        final List<String> commands = new ArrayList<>();

        for (final String function : functionNames) {
            final String logFile = logFilesMap.get(function);

            commands.add("start cmd /C java -jar " + this.seMoDeJarLocation + " googlePerformanceData " + function + " " + startTime + " " + endTime + " " + "../benchmarkingCommands/logs/" + logFile);
        }

        return commands;
    }

    private List<String> getIbmFetchCommands(final List<String> functionNames, final Map<String, String> logFilesMap,
                                             final String startTime, final String endTime, final String authorizationKey, final String namespace) {

        final List<String> commands = new ArrayList<>();

        for (final String function : functionNames) {
            final String logFile = logFilesMap.get(function);

            commands.add("start cmd /C java -jar " + this.seMoDeJarLocation + " openWhiskPerformanceData " + namespace + " " + function + "-dev-" + function + " " + authorizationKey + " " + startTime + " " + endTime + " " + "../benchmarkingCommands/logs/" + logFile);
        }

        return commands;
    }

    private List<String> getAzureFetchCommands(final List<String> serviceNames, final Path benchmarkLogsFolder,
                                               final Path apiKeysFolder, final Map<String, String> logFilesMap, final String logPrefix, final Optional<String> functionName, final String startTime, final String endTime)
            throws IOException {
        final List<String> commands = new ArrayList<>();
        final Map<String, String[]> apiKeysMap = this.getApiKeysMap(apiKeysFolder);
        if (apiKeysMap.size() != serviceNames.size()) {
            throw new IllegalArgumentException("number of function names (" + serviceNames.size()
                    + ") must be the same length as number of api keys (" + apiKeysMap.size() + ")");
        }
        for (final String serviceName : serviceNames) {
            final String[] apiKey = apiKeysMap.get(serviceName);
            Objects.requireNonNull(apiKey, "Api key does not exists. Wrong folder entered");

            final String logFile = logFilesMap.get(serviceName);
            Objects.requireNonNull(logFile, "LogFile does not exist. There exists a mismatch in the input files.");

            // hard coded relative paths, because also the folder structure is created in this prototype
            // there should be no change due to the folder structure
            if (functionName.isPresent()) {
                commands.add("start cmd /C java -jar " + this.seMoDeJarLocation + " azurePerformanceData " + apiKey[0] + " "
                        + apiKey[1] + " " + serviceName + " " + functionName.get() + " " + startTime + " " + endTime
                        + " " + "../benchmarkingCommands/logs/" + logFile);
            } else {
                commands.add("start cmd /C java -jar " + this.seMoDeJarLocation + " azurePerformanceData " + apiKey[0] + " "
                        + apiKey[1] + " " + serviceName + " " + serviceName + " " + startTime + " " + endTime + " "
                        + "../benchmarkingCommands/logs/" + logFile);
            }
        }
        return commands;
    }

    private Map<String, String[]> getApiKeysMap(final Path apiKeysFolder) throws IOException {
        final Map<String, String[]> map = new HashMap<>();

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(apiKeysFolder)) {
            for (final Path entry : stream) {
                final String fileName = entry.getFileName().toString();
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
     * @param logsFolder Must be an existing folder with *.log files
     * @return Map
     * @throws IOException
     */
    private Map<String, String> getBenchmarkLogsMap(final String provider) throws IOException {
        final int prefixLength = "benchmarking_MM-dd-HH-mm-ss_".length() + provider.length() + "_".length();
        final Map<String, String> map = new HashMap<>();

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(this.pathToBenchmarkLogs, "*.log")) {
            for (final Path entry : stream) {
                final String fileName = entry.getFileName().toString();
                final String functionName = fileName.substring(prefixLength, fileName.length() - ".log".length());
                map.put(functionName, fileName);
            }
        }

        return map;
    }
}
