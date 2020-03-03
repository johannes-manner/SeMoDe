package de.uniba.dsg.serverless.pipeline.controller;

import com.google.gson.GsonBuilder;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import de.uniba.dsg.serverless.pipeline.model.ProviderConfig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PipelineSetupController {

    private static final Logger logger = LogManager.getLogger(PipelineSetupController.class.getName());

    private final ObjectMapper om;
    private final PipelineSetup setup;

    private PipelineSetupController(final PipelineSetup setup) {
        this.setup = setup;
        this.om = new ObjectMapper();
    }

    public static PipelineSetupController init(final PipelineSetup setup) throws SeMoDeException {
        final PipelineSetupController controller = new PipelineSetupController(setup);
        controller.createBenchmarkFolderStructure();
        return controller;
    }

    public static PipelineSetupController load(final PipelineSetup setup) throws SeMoDeException {
        final PipelineSetupController controller = new PipelineSetupController(setup);
        if (!Files.isDirectory(setup.pathToSetup)) {
            throw new SeMoDeException("Test setup does not exist.");
        }
        setup.loadUserConfig(setup.pathToConfig.toString());
        return controller;
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
        do {
            System.out.println("Insert a valid provider: " + this.setup.possibleProviders.keySet().toString());
            provider = PipelineSetupUtility.scanner.nextLine();
            if (this.setup.possibleProviders.containsKey(provider)) {
                final List<String> providerJson = this.readProviderProperties(provider);
                final String json = "{" + providerJson.stream().collect(Collectors.joining(",")) + "}";
                try {
                    final ProviderConfig p = this.om.readValue(json, ProviderConfig.class);
                    p.validate(this.setup.possibleProviders);
                    this.setup.userProviders.put(p.getName(), p);

                    // auto save
                    this.savePipelineSetup();
                } catch (final IOException e) {
                    System.err.println("Incorrect json format: " + json);
                } catch (final SeMoDeException e) {
                    System.err.println("Incorrect property value: " + e.getMessage());
                }
            }
        } while (!"".equals(provider));

    }

    private List<String> readProviderProperties(final String provider) {

        final List<String> providerProperties = new ArrayList<>();
        for (final String key : ProviderConfig.jsonProviderProperties()) {
            this.printPropertyPrompt(key);
            final String line = PipelineSetupUtility.scanner.nextLine();
            providerProperties.add("\"" + key + "\": " + line);
            logger.info("Successfully stored property " + key);
        }
        return providerProperties;
    }

    private void printPropertyPrompt(final String key) {
        System.out.println("Configure property \"" + key + "\"");
        System.out.println(
                "Please specify the property. Think about the correct JSON representation for the value. \n(empty to skip property)");
    }

    private void savePipelineSetup() throws SeMoDeException {

        try {
            this.setup.updateUserConfig();
            this.om.writer().withDefaultPrettyPrinter().writeValue(Paths.get(this.setup.pathToConfig.toString()).toFile(),
                    this.setup.userConfig);
        } catch (final IOException e) {
            throw new SeMoDeException("Configuration could not be saved.", e);
        }

    }

    public void printPipelineSetupStatus() throws SeMoDeException {
        System.out.println("Printing status of pipeline setup \"" + this.setup.name + "\"");
        System.out.println("Printing Properties:");
        this.setup.updateUserConfig();

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(this.setup.userConfig));

    }

    public void prepareDeployment() throws SeMoDeException {

        System.out.println("copying sources...");
        for (final String provider : this.setup.userProviders.keySet()) {
            for (final String language : this.setup.userProviders.get(provider).getLanguage()) {

                // copies the sources from the fibonacci folder into the specific setup config
                this.copySource(provider, language);

                // change parameters in createDeployments bashscripts
                this.changeDeploymentParameters(this.setup.userProviders.get(provider), language);

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
        for (final String provider : this.setup.userProviders.keySet()) {
            for (final String language : this.setup.userProviders.get(provider).getLanguage()) {
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
        for (final String provider : this.setup.userProviders.keySet()) {
            for (final String language : this.setup.userProviders.get(provider).getLanguage()) {
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
        this.setup.benchmarkConfig = config;

        // auto save to store the benchmark
        this.savePipelineSetup();

        final BenchmarkingCommandGenerator bcg = new BenchmarkingCommandGenerator(this.setup.pathToBenchmarkingCommands, this.setup.pathToEndpoints, this.setup.benchmarkConfig, this.setup.getSeMoDeJarLocation());

        for (final String provider : this.setup.userProviders.keySet()) {
            for (final String language : this.setup.userProviders.get(provider).getLanguage()) {
                bcg.generateCommands(language, provider);
            }
        }
    }

    public void fetchPerformanceData() throws SeMoDeException {

        final FetchingCommandGenerator fcg = new FetchingCommandGenerator(this.setup.pathToBenchmarkingCommands, this.setup.pathToFetchingCommands, this.setup.pathToEndpoints, this.setup.config.getLanguageConfigMap(), this.setup.getSeMoDeJarLocation());

        for (final String provider : this.setup.userProviders.keySet()) {
            for (final String language : this.setup.userProviders.get(provider).getLanguage()) {
                fcg.fetchCommands(provider, language);
            }
        }
    }

    public void generateCalibration() throws SeMoDeException {
        String platform = "";
        final List<String> validPlatforms = List.of(CalibrationPlatform.values()).stream().map(CalibrationPlatform::getText).collect(Collectors.toList());
        while (!validPlatforms.contains(platform)) {
            System.out.println("Insert a possible calibration platform. Options: " + validPlatforms);
            platform = PipelineSetupUtility.scanner.nextLine();
        }


        if (platform.equals(CalibrationPlatform.LOCAL.getText())) {
            System.out.println("Current value for 'localSteps' is " + this.setup.userConfig.getCalibrationConfig().getLocalSteps() +
                    "\nInsert localSteps property (true or false) or skip setting: ");
            final String localSteps = PipelineSetupUtility.scanner.nextLine();
            System.out.println("Insert enabled property (true or false) or skip setting: ");
            final String enabled = PipelineSetupUtility.scanner.nextLine();

            this.setup.userConfig.updateLocalConfig(localSteps, enabled);

        } else if (platform.equals(CalibrationPlatform.AWS.getText())) {
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

            this.setup.userConfig.updateAWSConfig(targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled);

        }
        // auto save to store the pipeline setup
        this.savePipelineSetup();
    }

    public void startCalibration() {
        // TODO
    }
}
