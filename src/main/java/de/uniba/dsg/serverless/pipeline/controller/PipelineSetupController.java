package de.uniba.dsg.serverless.pipeline.controller;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.calibration.methods.AWSCalibration;
import de.uniba.dsg.serverless.calibration.methods.CalibrationMethods;
import de.uniba.dsg.serverless.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.pipeline.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.pipeline.model.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.apache.commons.lang3.tuple.Pair;

@Deprecated
public class PipelineSetupController {

    private final PipelineFileHandler setup;
    // wrapper/handler to access the user config
    private final SetupService setupService;
    private final Scanner scanner;
    private CalibrationMethods calibration;

    public PipelineSetupController(final PipelineFileHandler setup) {
        this.setup = setup;
        this.setupService = new SetupService();
        this.scanner = new Scanner(System.in);
    }

    public FileLogger getPipelineLogger() {
        return this.setup.getLogger();
    }

    private String scanAndLog() {
        final String enteredString = this.scanner.nextLine();
        this.setup.logger.info("Entered String: " + enteredString);
        return enteredString;
    }

    public void load() throws SeMoDeException {
        if (!Files.isDirectory(this.setup.pathToSetup)) {
            throw new SeMoDeException("Test setup does not exist.");
        }
        this.setupService.loadUserConfig(this.setup.pathToConfig.toString());
    }

//    private void createBenchmarkFolderStructure() throws SeMoDeException {
//        if (Files.exists(this.setup.pathToSetup)) {
//            throw new SeMoDeException("Test setup already exists. Choose a different name.");
//        }
//        try {
//            Files.createDirectories(this.setup.pathToSetup);
//            // for benchmarking
//            Files.createDirectories(this.setup.pathToBenchmarkExecution);
//            // for calibration
//            Files.createDirectories(this.setup.pathToCalibration);
//        } catch (final IOException e) {
//            throw new SeMoDeException(e);
//        }
//    }

    public void configureBenchmarkSetup() throws SeMoDeException {
//        String provider = "";
//        final List<String> validPlatforms = List.of(SupportedPlatform.values()).stream().map(SupportedPlatform::getText).collect(Collectors.toList());
//        while (!validPlatforms.contains(provider)) {
//            this.setup.logger.info("Insert a valid provider: " + validPlatforms);
//            provider = this.scanAndLog();
//        }
//
//        // the provider is already natively supported via its SDK supported.
//        if (SupportedPlatform.AWS.getText().equals(provider)) {
//
//            this.setup.logger.info("Insert aws function info:");
//            this.setup.logger.info("Insert current region or skip setting: ");
//            final String region = this.scanAndLog();
//            this.setup.logger.info("Insert runtime for benchmarking or skip setting: ");
//            final String runtime = this.scanAndLog();
//            this.setup.logger.info("Insert function execution role (AWS IAM ARN) or skip setting: ");
//            final String awsArnRole = this.scanAndLog();
//            this.setup.logger.info("Insert function handler here or skip setting: ");
//            final String functionHandler = this.scanAndLog();
//            this.setup.logger.info("Insert timeout for function handler or skip setting: ");
//            final String timeout = this.scanAndLog();
//            this.setup.logger.info("Insert current memorySizes (JSON Array) or skip setting: ");
//            final String memorySizes = this.scanAndLog();
//            this.setup.logger.info("Insert path to function source code (directory) or skip setting: ");
//            final String pathToSource = this.scanAndLog();
//
//            this.setup.logger.info("Insert additional info, otherwise these fields are automatically configured during deployment!");
//            this.setup.logger.info("Insert current target url or skip setting: ");
//            final String targetUrl = this.scanAndLog();
//            this.setup.logger.info("Insert current apiKey or skip setting: ");
//            final String apiKey = this.scanAndLog();
//
//            this.setupService.updateAWSFunctionBenchmarkConfig(region, runtime, awsArnRole, functionHandler, timeout, memorySizes, pathToSource, targetUrl, apiKey);
//        } else {
//            // TODO change all providers to native sdks - legacy code
//        }
//
//        // global benchmark parameters
//        // TODO check if this is really needed - check the notes
//        this.setup.logger.info("Global benchmarking parameters:");
//        this.setup.logger.info("Insert number of threads or skip setting:");
//        final String numberOfThreads = this.scanAndLog();
////        this.setup.logger.info("Insert a supported benchmarking mode or skip setting. Options: "
////                + List.of(BenchmarkMode.values()).stream().map(BenchmarkMode::getText).collect(Collectors.toList()));
//        this.setup.logger.info("Usage for each mode:\n"
//                + "\tconcurrent NUMBER_OF_THREADS NUMBER_OF_REQUESTS\n"
//                + "\tsequentialInterval NUMBER_OF_THREADS NUMBER_OF_REQUESTS DELAY\n"
//                + "\tsequentialWait NUMBER_OF_THREADS NUMBER_OF_REQUESTS DELAY\n"
//                + "\tsequentialConcurrent NUMBER_OF_THREADS NUMBER_OF_GROUPS NUMBER_OF_REQUESTS_GROUP DELAY\n"
//                + "\tsequentialChangingInterval NUMBER_OF_THREADS NUMBER_OF_REQUESTS (DELAY)+\n"
//                + "\tsequentialChangingWait NUMBER_OF_THREADS NUMBER_OF_REQUESTS (DELAY)+\n"
//                + "\tarbitraryLoadPattern NUMBER_OF_THREADS FILE.csv");
//        final String benchmarkingMode = this.scanAndLog();
//        this.setup.logger.info("Insert benchmarking parameters or skip setting:");
//        final String benchmarkingParameters = this.scanAndLog();
//        this.setup.logger.info("Insert a static value (POST argument for the http call) for benchmarking the function or skip setting:");
//        final String postArgument = this.scanAndLog();
//
//        this.setupService.updateGlobalBenchmarkParameters(numberOfThreads, benchmarkingMode, benchmarkingParameters, postArgument);
    }

    public void savePipelineSetup() throws SeMoDeException {
        this.setupService.saveUserConfigToFile(this.setup.pathToConfig);
    }

    public void printPipelineSetupStatus() throws SeMoDeException {
        this.setup.logger.info("Printing status of pipeline setup \"" + this.setup.name + "\"");
        this.setup.logger.info("Printing Properties:");

        this.setup.logger.info(this.setupService.getPrintableString());
    }

    public void deployFunctions() throws SeMoDeException {
//        for (final BenchmarkMethods benchmark : this.setupService.createBenchmarkMethodsFromConfig(this.setup.name)) {
//            benchmark.deploy();
//        }
    }

    public void undeployBenchmark() throws SeMoDeException {
//        for (final BenchmarkMethods benchmark : this.setupService.createBenchmarkMethodsFromConfig(this.setup.name)) {
//            benchmark.undeploy();
//        }
    }

    /**
     * Logs the start and end time and stores it in the user config. Needed for a later retrieval, see {@link
     * #fetchBenchmarkData()}.
     */
    public void executeBenchmark() throws SeMoDeException {
        this.setupService.logBenchmarkStartTime();

        final BenchmarkExecutor benchmarkExecutor = new BenchmarkExecutor(this.setup.pathToBenchmarkExecution, this.setupService.getBenchmarkConfig());
        benchmarkExecutor.generateLoadPattern();
//        benchmarkExecutor.executeBenchmark(this.setupService.createBenchmarkMethodsFromConfig(this.setup.name));

        this.setupService.logBenchmarkEndTime();
    }

    public void fetchBenchmarkData() throws SeMoDeException {
        final Pair<LocalDateTime, LocalDateTime> startEndTime = this.setupService.getStartAndEndTime();
//        for (final BenchmarkMethods benchmark : this.setupService.createBenchmarkMethodsFromConfig(this.setup.name)) {
//            benchmark.writePerformanceDataToFile(this.setup.pathToBenchmarkExecution, startEndTime.getLeft(), startEndTime.getRight());
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
            this.setup.logger.info("Insert dockerSourceFolder property or skip setting: ");
            final String dockerSourceFolder = this.scanAndLog();

            this.setupService.updateLocalConfig(localSteps, numberOfLocalCalibrations, enabled, dockerSourceFolder);
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

            this.setupService.updateAWSConfig(region, runtime, awsArnRole, functionHandler, timeout, deployLinpack, targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled, pathToSource);
        }
    }

    /**
     * Starts the calibration within the running program via the pipeline.
     */
    public void deployCalibration() throws SeMoDeException {
        if (this.setupService.isLocalEnabled()) {
            this.calibration = new LocalCalibration(this.setup.name, this.setup.pathToCalibration, this.setupService.getLocalConfig());
            this.calibration.deployCalibration();
        } else if (this.setupService.isAWSEnabled()) {
            this.calibration = new AWSCalibration(this.setup.name, this.setup.pathToCalibration, this.setupService.getAWSConfig());
            this.calibration.deployCalibration();
        }
    }

    public void startCalibration() throws SeMoDeException {
        if (this.setupService.isLocalEnabled()) {
            this.calibration = new LocalCalibration(this.setup.name, this.setup.pathToCalibration, this.setupService.getLocalConfig());
            this.calibration.startCalibration();
        } else if (this.setupService.isAWSEnabled()) {
            this.calibration = new AWSCalibration(this.setup.name, this.setup.pathToCalibration, this.setupService.getAWSConfig());
            this.calibration.startCalibration();
        }
    }

    /**
     * Stops the calibration and undeploys the already deployed resources.
     */
    public void undeployCalibration() throws SeMoDeException {
        if (this.setupService.isLocalEnabled()) {
            this.calibration = new LocalCalibration(this.setup.name, this.setup.pathToCalibration, this.setupService.getLocalConfig());
            this.calibration.stopCalibration();
        } else if (this.setupService.isAWSEnabled()) {
            this.calibration = new AWSCalibration(this.setup.name, this.setup.pathToCalibration, this.setupService.getAWSConfig());
            this.calibration.stopCalibration();
        }
    }

    public void computeMapping() throws SeMoDeException {
        this.setup.logger.info("Insert mapping info:");
        this.setup.logger.info("Insert local calibration.csv file path or skip setting: ");
        final String localCalibrationFile = this.scanAndLog();
        this.setup.logger.info("Insert provider calibration.csv file path or skip setting: ");
        final String providerCalibrationFile = this.scanAndLog();
        this.setup.logger.info("Insert memory settings (JSON Array) for computing the cpu share: ");
        final String memoryJSON = this.scanAndLog();

        this.setupService.updateMappingConfig(localCalibrationFile, providerCalibrationFile, memoryJSON);

        new MappingMaster(this.setupService.getMappingConfig(), this.getPipelineLogger()).computeMapping();
    }

    public void runLocalContainer() throws SeMoDeException {
        this.setup.logger.info("Insert running local container info:");
        this.setup.logger.info("Insert dockerSourceFolder property or skip setting: ");
        final String dockerSourceFolder = this.scanAndLog();
        this.setup.logger.info("Insert environment variables file or skip setting: ");
        final String environmentVariablesFile = this.scanAndLog();
        this.setup.logger.info("Insert number of profiles");
        final String numberOfProfiles = this.scanAndLog();

        this.setupService.updateRunningConfig(dockerSourceFolder, environmentVariablesFile, numberOfProfiles);

        final ContainerExecutor containerExecutor = new ContainerExecutor(this.setup.pathToCalibration, this.setupService.getMappingConfig(), this.setupService.getRunningConfig(), this.getPipelineLogger());
        containerExecutor.executeLocalProfiles();
    }
}
