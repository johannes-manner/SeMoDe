package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.calibration.methods.AWSCalibration;
import de.uniba.dsg.serverless.calibration.methods.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.ProviderConfig;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class PipelineSetupController {

    private final ObjectMapper om;
    private final PipelineSetup setup;
    // wrapper/handler to access the user config
    private final UserConfigHandler userConfigHandler;
    private final Scanner scanner;
    private CalibrationMethods calibration;

    public PipelineSetupController(final PipelineSetup setup) {
        this.setup = setup;
        this.om = new ObjectMapper();
        this.userConfigHandler = new UserConfigHandler();
        this.scanner = new Scanner(System.in);
    }

    public FileLogger getPipelineLogger() {
        return this.setup.logger;
    }

    private String scanAndLog() {
        final String enteredString = this.scanner.nextLine();
        this.setup.logger.info("Entered String: " + enteredString);
        return enteredString;
    }

    /**
     * Initializes the user config with some default values which are helpful, e.g. the calibration options,
     * if some parameters should be unchanged to the global config.
     *
     * @return
     */
    public void init() throws SeMoDeException {
        this.createBenchmarkFolderStructure();
        this.userConfigHandler.initializeCalibrationFromGlobal(this.setup.globalConfig);
        this.savePipelineSetup();
    }

    public void load() throws SeMoDeException {
        if (!Files.isDirectory(this.setup.pathToSetup)) {
            throw new SeMoDeException("Test setup does not exist.");
        }
        this.userConfigHandler.loadUserConfig(this.setup.pathToConfig.toString());
    }

    private void createBenchmarkFolderStructure() throws SeMoDeException {
        if (Files.exists(this.setup.pathToSetup)) {
            throw new SeMoDeException("Test setup already exists. Choose a different name.");
        }
        try {
            Files.createDirectories(this.setup.pathToSetup);
            // for benchmarking
            Files.createDirectories(this.setup.pathToBenchmarkExecution);
            // for calibration
            Files.createDirectories(this.setup.pathToCalibration);
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }

    /**
     * As in 2018, the first prototype to benchmark functions was implemented during the project
     * in the summer term. All information was created in bash files and executed from there.
     * <br/>
     * In 2020, the procedure changed, started with AWS, that native SDKs should be used for getting
     * the information. The benchmarking pipeline is reimplemented due to this decision and other
     * providers and open source FaaS platforms should follow.
     */
    public void configureBenchmarkSetup() throws SeMoDeException {
        String provider = "";
        final Map<String, ProviderConfig> validProviders = this.setup.globalConfig.getProviderConfigMap();
        while (!validProviders.containsKey(provider)) {
            this.setup.logger.info("Insert a valid provider: " + validProviders.keySet().toString());
            provider = this.scanAndLog();
        }

        // the provider is already natively supported via its SDK supported.
        if (SupportedPlatform.AWS.getText().equals(provider)) {

            this.setup.logger.info("Insert aws function info:");
            this.setup.logger.info("Insert current region or skip setting: ");
            final String region = this.scanAndLog();
            this.setup.logger.info("Insert runtime for benchmarking or skip setting: ");
            final String runtime = this.scanAndLog();
            this.setup.logger.info("Insert function execution role (AWS IAM ARN) or skip setting: ");
            final String awsArnRole = this.scanAndLog();
            this.setup.logger.info("Insert function handler here or skip setting: ");
            final String functionHandler = this.scanAndLog();
            this.setup.logger.info("Insert timeout for function handler or skip setting: ");
            final String timeout = this.scanAndLog();
            this.setup.logger.info("Insert current memorySizes (JSON Array) or skip setting: ");
            final String memorySizes = this.scanAndLog();
            this.setup.logger.info("Insert path to function source code (directory) or skip setting: ");
            final String pathToSource = this.scanAndLog();

            this.setup.logger.info("Insert additional info, otherwise these fields are automatically configured during deployment!");
            this.setup.logger.info("Insert current target url or skip setting: ");
            final String targetUrl = this.scanAndLog();
            this.setup.logger.info("Insert current apiKey or skip setting: ");
            final String apiKey = this.scanAndLog();

            this.userConfigHandler.updateAWSFunctionBenchmarkConfig(region, runtime, awsArnRole, functionHandler, timeout, memorySizes, pathToSource, targetUrl, apiKey);

        } else {
            // TODO change all providers to native sdks - legacy code
            try {
                this.setup.logger.info("Insert memory sizes (JSON Array) or skip setting: ");
                final String memorySize = this.scanAndLog();
                this.setup.logger.info("Insert languages (JSON Array), e.g. [\"java\"] or skip setting: ");
                final String language = this.scanAndLog();
                this.setup.logger.info("Insert deployment sizes (JSON Array) or skip setting: ");
                final String deploymentSize = this.scanAndLog();

                this.userConfigHandler.addOrChangeProviderConfig(this.setup.globalConfig.getProviderConfigMap(), provider, memorySize, language, deploymentSize);
            } catch (final IOException e) {
                this.setup.logger.warning("Incorrect json format - inserted values!");
            } catch (final SeMoDeException e) {
                this.setup.logger.warning("Incorrect property value: " + e.getMessage());
            }
        }

        // global benchmark parameters
        // TODO check if this is really needed - check the notes
        this.setup.logger.info("Global benchmarking parameters:");
        this.setup.logger.info("Insert number of threads or skip setting:");
        final String numberOfThreads = this.scanAndLog();
        this.setup.logger.info("Insert a supported benchmarking mode or skip setting. Options: "
                + List.of(BenchmarkMode.values()).stream().map(BenchmarkMode::getText).collect(Collectors.toList()));
        this.setup.logger.info("Usage for each mode:\n"
                + "\tconcurrent NUMBER_OF_THREADS NUMBER_OF_REQUESTS\n"
                + "\tsequentialInterval NUMBER_OF_THREADS NUMBER_OF_REQUESTS DELAY\n"
                + "\tsequentialWait NUMBER_OF_THREADS NUMBER_OF_REQUESTS DELAY\n"
                + "\tsequentialConcurrent NUMBER_OF_THREADS NUMBER_OF_GROUPS NUMBER_OF_REQUESTS_GROUP DELAY\n"
                + "\tsequentialChangingInterval NUMBER_OF_THREADS NUMBER_OF_REQUESTS (DELAY)+\n"
                + "\tsequentialChangingWait NUMBER_OF_THREADS NUMBER_OF_REQUESTS (DELAY)+\n"
                + "\tarbitraryLoadPattern NUMBER_OF_THREADS FILE.csv");
        final String benchmarkingMode = this.scanAndLog();
        this.setup.logger.info("Insert benchmarking parameters or skip setting:");
        final String benchmarkingParameters = this.scanAndLog();
        this.setup.logger.info("Insert a static value (POST argument for the http call) for benchmarking the function or skip setting:");
        final String postArgument = this.scanAndLog();

        this.userConfigHandler.updateGlobalBenchmarkParameters(numberOfThreads, benchmarkingMode, benchmarkingParameters, postArgument);

    }

    public void savePipelineSetup() throws SeMoDeException {
        this.userConfigHandler.saveUserConfigToFile(this.setup.pathToConfig);
    }

    public void printPipelineSetupStatus() throws SeMoDeException {
        this.setup.logger.info("Printing status of pipeline setup \"" + this.setup.name + "\"");
        this.setup.logger.info("Printing Properties:");

        this.setup.logger.info(this.userConfigHandler.getPrintableString());

    }

    public void deployFunctions() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.userConfigHandler.createBenchmarkMethodsFromConfig(this.setup.name)) {
            benchmark.deploy();
        }
    }

    public void undeployBenchmark() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.userConfigHandler.createBenchmarkMethodsFromConfig(this.setup.name)) {
            benchmark.undeploy();
        }
    }

    private void changeDeploymentParameters(final ProviderConfig providerConfig, final String language) throws SeMoDeException {
        final String sourceFolderName = providerConfig.getName() + "-" + language;
        final Path createDeployments = Paths.get(this.setup.pathToSources.toString(), sourceFolderName, "createDeployments");

        try {
            String content = new String(Files.readAllBytes(createDeployments));
            content = content.replaceAll("DEPLOYMENT_SIZES", providerConfig.getDeploymentSize().stream().map(i -> i.toString()).collect(Collectors.joining(" ")));
            content = content.replaceAll("MEMORY_SIZES", providerConfig.getMemorySize().stream().map(i -> i.toString()).collect(Collectors.joining(" ")));
            Files.write(createDeployments, content.getBytes());
        } catch (final IOException e) {
            throw new SeMoDeException("File is not readable or writable: " + createDeployments.toString(), e);
        }
    }

    /**
     * Logs the start and end time and stores it in the user config.
     * Needed for a later retrieval, see {@link #fetchBenchmarkData()}.
     */
    public void executeBenchmark() throws SeMoDeException {
        this.userConfigHandler.logBenchmarkStartTime();

        final BenchmarkExecutor benchmarkExecutor = new BenchmarkExecutor(this.setup.pathToBenchmarkExecution, this.userConfigHandler.getBenchmarkConfig());
        benchmarkExecutor.generateLoadPattern();
        benchmarkExecutor.executeBenchmark(this.userConfigHandler.createBenchmarkMethodsFromConfig(this.setup.name));

        this.userConfigHandler.logBenchmarkEndTime();
    }

    public void fetchBenchmarkData() throws SeMoDeException {
        final Pair<LocalDateTime, LocalDateTime> startEndTime = this.userConfigHandler.getStartAndEndTime();
        for (final BenchmarkMethods benchmark : this.userConfigHandler.createBenchmarkMethodsFromConfig(this.setup.name)) {
            benchmark.writePerformanceDataToFile(this.setup.pathToBenchmarkExecution, startEndTime.getLeft(), startEndTime.getRight());
        }
    }

    @Deprecated
    public void fetchPerformanceData() throws SeMoDeException {

//        final FetchingCommandGenerator fcg = new FetchingCommandGenerator(this.setup.pathToBenchmarkExecution, this.setup.pathToFetchingCommands, this.setup.pathToEndpoints, this.setup.globalConfig.getLanguageConfigMap(), this.setup.getSeMoDeJarLocation());
//        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
//        for (final String provider : userProviders.keySet()) {
//            for (final String language : userProviders.get(provider).getLanguage()) {
//                fcg.fetchCommands(provider, language);
//            }
//        }
    }

    public void configureCalibration() throws SeMoDeException {
        String platform = "";
        final List<String> validPlatforms = List.of(SupportedPlatform.values()).stream().map(SupportedPlatform::getText).collect(Collectors.toList());
        while (!validPlatforms.contains(platform)) {
            this.setup.logger.info("Insert a possible calibration platform. Options: " + validPlatforms);
            platform = this.scanAndLog();
        }

        if (platform.equals(SupportedPlatform.LOCAL.getText())) {
            this.setup.logger.info("Insert localSteps property or skip setting: ");
            final String localSteps = this.scanAndLog();
            this.setup.logger.info("Insert numberOfLocalCalibrations property or skip setting: ");
            final String numberOfLocalCalibrations = this.scanAndLog();
            this.setup.logger.info("Insert enabled property (true or false) or skip setting: ");
            final String enabled = this.scanAndLog();

            this.userConfigHandler.updateLocalConfig(localSteps, numberOfLocalCalibrations, enabled);

        } else if (platform.equals(SupportedPlatform.AWS.getText())) {
            this.setup.logger.info("Insert calibration info:");
            this.setup.logger.info("Insert true or false, if you want to deploy linpack or skip setting: ");
            final String deployLinpack = this.scanAndLog();
            this.setup.logger.info("Insert current bucketName or skip setting: ");
            final String bucketName = this.scanAndLog();
            this.setup.logger.info("Insert current number of executions or skip setting: ");
            final String numberOfAWSExecutions = this.scanAndLog();
            this.setup.logger.info("Insert enabled property (true or false) or skip setting: ");
            final String enabled = this.scanAndLog();

            this.setup.logger.info("Insert calibration function info:");
            this.setup.logger.info("Insert current region or skip setting: ");
            final String region = this.scanAndLog();
            this.setup.logger.info("Insert runtime for calibration or skip setting: ");
            final String runtime = this.scanAndLog();
            this.setup.logger.info("Insert function execution role (AWS IAM ARN) or skip setting: ");
            final String awsArnRole = this.scanAndLog();
            this.setup.logger.info("Insert function handler here or skip setting: ");
            final String functionHandler = this.scanAndLog();
            this.setup.logger.info("Insert timeout for function handler or skip setting: ");
            final String timeout = this.scanAndLog();
            this.setup.logger.info("Insert current memorySizes (JSON Array) or skip setting: ");
            final String memorySizes = this.scanAndLog();
            this.setup.logger.info("Insert path to function source code (directory) or skip setting: ");
            final String pathToSource = this.scanAndLog();

            this.setup.logger.info("Insert additional info, otherwise these fields are automatically configured during deployment!");
            this.setup.logger.info("Insert current target url or skip setting: ");
            final String targetUrl = this.scanAndLog();
            this.setup.logger.info("Insert current apiKey or skip setting: ");
            final String apiKey = this.scanAndLog();

            this.userConfigHandler.updateAWSConfig(region, runtime, awsArnRole, functionHandler, timeout, deployLinpack, targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled, pathToSource);

        }
    }

    /**
     * Starts the calibration within the running program via the pipeline.
     *
     * @throws SeMoDeException
     */
    public void startCalibration() throws SeMoDeException {
        if (this.userConfigHandler.isLocalEnabled()) {
            this.calibration = new LocalCalibration(this.setup.name, this.setup.pathToCalibration, this.userConfigHandler.getLocalConfig());
            this.calibration.performCalibration();
        } else if (this.userConfigHandler.isAWSEnabled()) {
            this.calibration = new AWSCalibration(this.setup.name, this.setup.pathToCalibration, this.userConfigHandler.getAWSConfig());
            this.calibration.performCalibration();
        }
    }

    /**
     * Stops the calibration and undeploys the already deployed resources.
     *
     * @throws SeMoDeException
     */
    public void undeployCalibration() throws SeMoDeException {
        if (this.userConfigHandler.isLocalEnabled()) {
            this.calibration = new LocalCalibration(this.setup.name, this.setup.pathToCalibration, this.userConfigHandler.getLocalConfig());
            this.calibration.stopCalibration();
        } else if (this.userConfigHandler.isAWSEnabled()) {
            this.calibration = new AWSCalibration(this.setup.name, this.setup.pathToCalibration, this.userConfigHandler.getAWSConfig());
            this.calibration.stopCalibration();
        }
    }
}
