package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;
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

public class BenchmarkSetupController {

    private static final Logger logger = LogManager.getLogger(BenchmarkSetupController.class.getName());

    private final ObjectMapper om;
    private final BenchmarkSetup setup;

    private BenchmarkSetupController(final BenchmarkSetup setup) {
        this.setup = setup;
        this.om = new ObjectMapper();
    }

    public static BenchmarkSetupController init(final BenchmarkSetup setup) throws SeMoDeException {
        final BenchmarkSetupController controller = new BenchmarkSetupController(setup);
        controller.createBenchmarkFolderStructure();
        return controller;
    }

    public static BenchmarkSetupController load(final BenchmarkSetup setup) throws SeMoDeException {
        final BenchmarkSetupController controller = new BenchmarkSetupController(setup);
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
            Files.createDirectories(this.setup.pathToDeployment);
            Files.createDirectories(this.setup.pathToEndpoints);
            Files.createDirectories(this.setup.pathToBenchmarkingCommands);
            Files.createDirectories(this.setup.pathToFetchingCommands);
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
                    this.saveBenchmarkSetup();
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

    private void saveBenchmarkSetup() throws SeMoDeException {

        try {
            this.om.writer().withDefaultPrettyPrinter().writeValue(Paths.get(this.setup.pathToConfig.toString()).toFile(),
                    this.setup.assembleUserConfig());
        } catch (final IOException e) {
            throw new SeMoDeException("Configuration could not be saved.", e);
        }

    }

    public void printBenchmarkSetupStatus() throws SeMoDeException {
        System.out.println("Printing status of benchmark setup \"" + this.setup.name + "\"");
        System.out.println("Printing Properties:");
        System.out.println(this.setup.assembleUserConfig());
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
        this.saveBenchmarkSetup();

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
}
