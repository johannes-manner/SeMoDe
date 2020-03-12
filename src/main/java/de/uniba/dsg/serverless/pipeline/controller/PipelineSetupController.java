package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.calibration.CalibrationMethods;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibration;
import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.ProviderConfig;
import de.uniba.dsg.serverless.pipeline.utils.BenchmarkingCommandGenerator;
import de.uniba.dsg.serverless.pipeline.utils.EndpointExtractor;
import de.uniba.dsg.serverless.pipeline.utils.FetchingCommandGenerator;
import de.uniba.dsg.serverless.util.SeMoDeProperty;
import de.uniba.dsg.serverless.util.SeMoDePropertyManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
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
        this.userConfigHandler.initializeCalibrationFromGlobal(this.setup.config);
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
            Files.createDirectories(this.setup.pathToDeployment);
            Files.createDirectories(this.setup.pathToEndpoints);
            Files.createDirectories(this.setup.pathToBenchmarkingCommands);
            Files.createDirectories(this.setup.pathToFetchingCommands);
            // for calibration
            Files.createDirectories(this.setup.pathToCalibration);
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }

    public void configureBenchmarkSetup() {
        String provider;
        final Map<String, ProviderConfig> validProviders = this.setup.config.getProviderConfigMap();
        do {
            System.out.println("Insert a valid provider: " + validProviders.keySet().toString());
            provider = PipelineSetupUtility.scanner.nextLine();
            if (validProviders.containsKey(provider)) {
                try {
                    System.out.println("Insert memory sizes (JSON Array) or skip setting: ");
                    final String memorySize = PipelineSetupUtility.scanner.nextLine();
                    System.out.println("Insert languages (JSON Array), e.g. [\"java\"] or skip setting: ");
                    final String language = PipelineSetupUtility.scanner.nextLine();
                    System.out.println("Insert deployment sizes (JSON Array) or skip setting: ");
                    final String deploymentSize = PipelineSetupUtility.scanner.nextLine();

                    this.userConfigHandler.addOrChangeProviderConfig(this.setup.config.getProviderConfigMap(), provider, memorySize, language, deploymentSize);
                } catch (final IOException e) {
                    System.err.println("Incorrect json format - inserted values!");
                } catch (final SeMoDeException e) {
                    System.err.println("Incorrect property value: " + e.getMessage());
                }
            }
        } while (!"".equals(provider));

    }

    public void savePipelineSetup() throws SeMoDeException {
        this.userConfigHandler.saveUserConfigToFile(this.setup.pathToConfig);
    }

    public void printPipelineSetupStatus() throws SeMoDeException {
        System.out.println("Printing status of pipeline setup \"" + this.setup.name + "\"");
        System.out.println("Printing Properties:");

        System.out.println(this.userConfigHandler.getPrintableString());

    }

    public void prepareDeployment() throws SeMoDeException {
        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
        // TODO make source copying etc. configurable, document function implementation
        System.out.println("copying sources...");
        for (final String provider : userProviders.keySet()) {
            for (final String language : userProviders.get(provider).getLanguage()) {

                // copies the sources from the fibonacci folder into the specific setup config
                this.copySource(provider, language);

                // change parameters in createDeployments bashscripts
                this.changeDeploymentParameters(userProviders.get(provider), language);

            }
        }

        // TODO run a single process builder for each provider / language combination

        // create Deployments
        System.out.println("creating deployment sizes");
        this.executeBashCommand("bash createDeployments " + this.setup.getSeMoDeJarLocation(), "-preparation");

        // deployment
        System.out.println("Deploying created functions... (may take a while)");
        this.executeBashCommand("bash deploy", "-deploy");

    }

    public void undeploy() throws SeMoDeException {
        this.executeBashCommand("bash undeploy", "-undeploy");
    }

    private void executeBashCommand(final String command, final String fileSuffix) throws SeMoDeException {
        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
        for (final String provider : userProviders.keySet()) {
            for (final String language : userProviders.get(provider).getLanguage()) {
                final ProcessBuilder processBuilder = new ProcessBuilder(SeMoDePropertyManager.getInstance().getProperty(SeMoDeProperty.BASH_LOCATION), "-c", command);
                final String providerLanguage = provider + "-" + language;
                final Path sourceLocation = Paths.get(this.setup.pathToSetup.toString(), "sources", providerLanguage);
                processBuilder.directory(sourceLocation.toFile());
                processBuilder.redirectErrorStream(true);
                Process process = null;
                try {
                    process = processBuilder.start();
                    this.writeProcessOutputToFile(process, providerLanguage + fileSuffix);
                    final int errCode = process.waitFor();
                    System.out
                            .println("Executed without errors? " + (errCode == 0 ? "Yes" : "No(code=" + errCode + ")"));
                } catch (final IOException | InterruptedException e) {
                    e.printStackTrace();
                    process.destroy();
                }
            }
        }
    }

    private void writeProcessOutputToFile(final Process process, final String fileName) throws SeMoDeException {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
             final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(this.setup.pathToDeployment.resolve(fileName).toString())))) {

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Process output: " + line);
                bw.write(line + System.lineSeparator());
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new SeMoDeException("Error while writing the output of the deploymentscript to the file");
        }
    }

    private void copySource(final String provider, final String language) throws SeMoDeException {
        final String sourceFolderName = provider + "-" + language;
        // TODO enable other functions / folders than fibonacci
        final File source = new File(Paths.get("fibonacci", sourceFolderName).toString());
        final File target = new File(this.setup.pathToSources.resolve(sourceFolderName).toString());
        try {
            FileUtils.copyDirectory(source, target);
        } catch (final IOException e) {
            throw new SeMoDeException("Copying the source of " + sourceFolderName + "failed.", e);
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

    public void generateEndpoints() throws SeMoDeException {
        final EndpointExtractor endpointExtractor = new EndpointExtractor(this.setup.config.getLanguageConfigMap(), this.setup.pathToDeployment, this.setup.pathToEndpoints);
        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
        for (final String provider : userProviders.keySet()) {
            for (final String language : userProviders.get(provider).getLanguage()) {
                endpointExtractor.extractEndpoints(language, provider);
            }
        }
    }

    public void generateBenchmarkingCommands() throws SeMoDeException {
        // TODO make update of values (input parameters) more robust
        System.out.println("Insert number of threads");
        final String numberOfThreads = PipelineSetupUtility.scanner.nextLine();
        System.out.println("Insert a supported benchmarking mode");
        final String benchmarkingMode = PipelineSetupUtility.scanner.nextLine();
        System.out.println("Insert benchmarking parameters");
        final String benchmarkingParameters = PipelineSetupUtility.scanner.nextLine();
        System.out.println("Copy a file called 'params.json' in the 'benchmarkingCommands' folder");

        final BenchmarkConfig config = new BenchmarkConfig(numberOfThreads, benchmarkingMode, benchmarkingParameters);
        this.userConfigHandler.updateBenchmarkConfig(config);

        final BenchmarkingCommandGenerator bcg = new BenchmarkingCommandGenerator(this.setup.pathToBenchmarkingCommands, this.setup.pathToEndpoints, config, this.setup.getSeMoDeJarLocation());

        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
        for (final String provider : userProviders.keySet()) {
            for (final String language : userProviders.get(provider).getLanguage()) {
                bcg.generateCommands(language, provider);
            }
        }
    }

    public void fetchPerformanceData() throws SeMoDeException {

        final FetchingCommandGenerator fcg = new FetchingCommandGenerator(this.setup.pathToBenchmarkingCommands, this.setup.pathToFetchingCommands, this.setup.pathToEndpoints, this.setup.config.getLanguageConfigMap(), this.setup.getSeMoDeJarLocation());
        final Map<String, ProviderConfig> userProviders = this.userConfigHandler.getUserConfigProviders();
        for (final String provider : userProviders.keySet()) {
            for (final String language : userProviders.get(provider).getLanguage()) {
                fcg.fetchCommands(provider, language);
            }
        }
    }

    public void generateCalibration() throws SeMoDeException {
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
            System.out.println("Insert true or false, if you want to deploy linpack or skip setting: ");
            final String deployLinpack = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current target url or skip setting: ");
            final String targetUrl = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current apiKey or skip setting: ");
            final String apiKey = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current bucketName or skip setting: ");
            final String bucketName = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current memorySizes (JSON Array) or skip setting: ");
            final String memorySizes = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert current number of executions or skip setting: ");
            final String numberOfAWSExecutions = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert enabled property (true or false) or skip setting: ");
            final String enabled = PipelineSetupUtility.scanner.nextLine();

            this.userConfigHandler.updateAWSConfig(region, runtime, awsArnRole, functionHandler, timeout, deployLinpack, targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled);

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

    public void stopCalibration() throws SeMoDeException {
        if (this.userConfigHandler.isLocalEnabled()) {
            this.calibration = new LocalCalibration(this.setup.name, this.setup.pathToCalibration, this.userConfigHandler.getLocalConfig());
            this.calibration.stopCalibration();
        } else if (this.userConfigHandler.isAWSEnabled()) {
            this.calibration = new AWSCalibration(this.setup.name, this.setup.pathToCalibration, this.userConfigHandler.getAWSConfig());
            this.calibration.stopCalibration();
        }
    }
}
