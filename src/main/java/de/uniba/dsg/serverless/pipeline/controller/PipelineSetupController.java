package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.benchmark.BenchmarkMethods;
import de.uniba.dsg.serverless.benchmark.BenchmarkMode;
import de.uniba.dsg.serverless.calibration.CalibrationMethods;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibration;
import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.ProviderConfig;
import de.uniba.dsg.serverless.pipeline.utils.BenchmarkingCommandGenerator;
import de.uniba.dsg.serverless.pipeline.utils.EndpointExtractor;
import de.uniba.dsg.serverless.pipeline.utils.FetchingCommandGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PipelineSetupController {

    private static final Logger logger = LogManager.getLogger(PipelineSetupController.class.getName());

    private final ObjectMapper om;
    private final PipelineSetup setup;
    // wrapper/handler to access the user config
    private final UserConfigHandler userConfigHandler;
    private CalibrationMethods calibration;

    public PipelineSetupController(final PipelineSetup setup) {
        this.setup = setup;
        this.om = new ObjectMapper();
        this.userConfigHandler = new UserConfigHandler();
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
     * <br/>
     * Therefore the {@link BenchmarkingCommandGenerator}, {@link EndpointExtractor} and {@link FetchingCommandGenerator}
     * are deprecated.
     */
    public void configureBenchmarkSetup() throws SeMoDeException {
        String provider = "";
        final Map<String, ProviderConfig> validProviders = this.setup.globalConfig.getProviderConfigMap();
        while (!validProviders.containsKey(provider)) {
            System.out.println("Insert a valid provider: " + validProviders.keySet().toString());
            provider = PipelineSetupUtility.scanner.nextLine();
        }

        // the provider is already natively supported via its SDK supported.
        if (SupportedPlatform.AWS.getText().equals(provider)) {

            System.out.println("Insert aws function info:");
            System.out.println("Insert current region or skip setting: ");
            final String region = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert runtime for calibration or skip setting: ");
            final String runtime = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert function execution role (AWS IAM ARN) or skip setting: ");
            final String awsArnRole = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert function handler here or skip setting: ");
            final String functionHandler = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert timeout for function handler or skip setting: ");
            final String timeout = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current memorySizes (JSON Array) or skip setting: ");
            final String memorySizes = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert path to function source code (directory) or skip setting: ");
            final String pathToSource = PipelineSetupUtility.scanner.nextLine();

            System.out.println("Insert additional info, otherwise these fields are automatically configured during deployment!");
            System.out.println("Insert current target url or skip setting: ");
            final String targetUrl = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current apiKey or skip setting: ");
            final String apiKey = PipelineSetupUtility.scanner.nextLine();

            this.userConfigHandler.updateAWSFunctionBenchmarkConfig(region, runtime, awsArnRole, functionHandler, timeout, memorySizes, pathToSource, targetUrl, apiKey);

        } else {
            // TODO change all providers to native sdks - legacy code
            try {
                System.out.println("Insert memory sizes (JSON Array) or skip setting: ");
                final String memorySize = PipelineSetupUtility.scanner.nextLine();
                System.out.println("Insert languages (JSON Array), e.g. [\"java\"] or skip setting: ");
                final String language = PipelineSetupUtility.scanner.nextLine();
                System.out.println("Insert deployment sizes (JSON Array) or skip setting: ");
                final String deploymentSize = PipelineSetupUtility.scanner.nextLine();

                this.userConfigHandler.addOrChangeProviderConfig(this.setup.globalConfig.getProviderConfigMap(), provider, memorySize, language, deploymentSize);
            } catch (final IOException e) {
                System.err.println("Incorrect json format - inserted values!");
            } catch (final SeMoDeException e) {
                System.err.println("Incorrect property value: " + e.getMessage());
            }
        }

        // global benchmark parameters
        // TODO check if this is really needed - check the notes
        System.out.println("Insert number of threads or skip setting:");
        final String numberOfThreads = PipelineSetupUtility.scanner.nextLine();
        System.out.println("Insert a supported benchmarking mode or skip setting. Options: "
                + List.of(BenchmarkMode.values()).stream().map(BenchmarkMode::getText).collect(Collectors.toList()));
        final String benchmarkingMode = PipelineSetupUtility.scanner.nextLine();
        System.out.println("Insert benchmarking parameters or skip setting:");
        final String benchmarkingParameters = PipelineSetupUtility.scanner.nextLine();
        System.out.println("Insert a static value (POST argument for the http call) for benchmarking the function or skip setting:");
        final String postArgument = PipelineSetupUtility.scanner.nextLine();

        this.userConfigHandler.updateGlobalBenchmarkParameters(numberOfThreads, benchmarkingMode, benchmarkingParameters, postArgument);

    }

    public void savePipelineSetup() throws SeMoDeException {
        this.userConfigHandler.saveUserConfigToFile(this.setup.pathToConfig);
    }

    public void printPipelineSetupStatus() throws SeMoDeException {
        System.out.println("Printing status of pipeline setup \"" + this.setup.name + "\"");
        System.out.println("Printing Properties:");

        System.out.println(this.userConfigHandler.getPrintableString());

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

    public void executeBenchmark() {
        this.userConfigHandler.logBenchmarkStartTime();
        final BenchmarkExecutor benchmarkExecutor = new BenchmarkExecutor();

    }

//    @Deprecated
//    public void generateBenchmarkingCommands() throws SeMoDeException {
//        // TODO make update of values (input parameters) more robust
//        System.out.println("Insert number of threads or skip setting:");
//        final String numberOfThreads = PipelineSetupUtility.scanner.nextLine();
//        System.out.println("Insert a supported benchmarking mode");
//        final String benchmarkingMode = PipelineSetupUtility.scanner.nextLine();
//        System.out.println("Insert benchmarking parameters");
//        final String benchmarkingParameters = PipelineSetupUtility.scanner.nextLine();
//        System.out.println("Copy a file called 'params.json' in the 'benchmarkingCommands' folder");
//
////        final BenchmarkConfig config = new BenchmarkConfig(numberOfThreads, benchmarkingMode, benchmarkingParameters);
////        this.userConfigHandler.updateBenchmarkConfig(config);
//
////        final BenchmarkingCommandGenerator bcg = new BenchmarkingCommandGenerator(this.setup.pathToBenchmarkingCommands, this.setup.pathToEndpoints, config, this.setup.getSeMoDeJarLocation());
//
//        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
//        for (final String provider : userProviders.keySet()) {
//            for (final String language : userProviders.get(provider).getLanguage()) {
//                bcg.generateCommands(language, provider);
//            }
//        }
//    }

    public void fetchBenchmarkData() {
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
            System.out.println("Insert a possible calibration platform. Options: " + validPlatforms);
            platform = PipelineSetupUtility.scanner.nextLine();
        }

        if (platform.equals(SupportedPlatform.LOCAL.getText())) {
            System.out.println("Insert localSteps property or skip setting: ");
            final String localSteps = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert numberOfLocalCalibrations property or skip setting: ");
            final String numberOfLocalCalibrations = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert enabled property (true or false) or skip setting: ");
            final String enabled = PipelineSetupUtility.scanner.nextLine();

            this.userConfigHandler.updateLocalConfig(localSteps, numberOfLocalCalibrations, enabled);

        } else if (platform.equals(SupportedPlatform.AWS.getText())) {
            System.out.println("Insert calibration info:");
            System.out.println("Insert true or false, if you want to deploy linpack or skip setting: ");
            final String deployLinpack = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current bucketName or skip setting: ");
            final String bucketName = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current number of executions or skip setting: ");
            final String numberOfAWSExecutions = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert enabled property (true or false) or skip setting: ");
            final String enabled = PipelineSetupUtility.scanner.nextLine();

            System.out.println("Insert calibration function info:");
            System.out.println("Insert current region or skip setting: ");
            final String region = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert runtime for calibration or skip setting: ");
            final String runtime = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert function execution role (AWS IAM ARN) or skip setting: ");
            final String awsArnRole = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert function handler here or skip setting: ");
            final String functionHandler = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert timeout for function handler or skip setting: ");
            final String timeout = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current memorySizes (JSON Array) or skip setting: ");
            final String memorySizes = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert path to function source code (directory) or skip setting: ");
            final String pathToSource = PipelineSetupUtility.scanner.nextLine();

            System.out.println("Insert additional info, otherwise these fields are automatically configured during deployment!");
            System.out.println("Insert current target url or skip setting: ");
            final String targetUrl = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current apiKey or skip setting: ");
            final String apiKey = PipelineSetupUtility.scanner.nextLine();

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
