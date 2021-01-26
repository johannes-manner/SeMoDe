package de.uniba.dsg.serverless.pipeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.calibration.local.LocalCalibrationConfig;
import de.uniba.dsg.serverless.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.calibration.provider.AWSCalibration;
import de.uniba.dsg.serverless.calibration.provider.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.AWSBenchmark;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.RunningCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.spring.repo.LocalRESTEventRepository;
import de.uniba.dsg.serverless.spring.repo.ProviderEventRepository;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper to change the attributes of the user config class. If the changes are made directly in the model classes,
 * there occur json parsing errors.
 */
@Slf4j
@Service
public class SetupService {

    // model
    private SetupConfig setupConfig;
    private PipelineFileHandler fileHandler;

    @Value("${semode.setups.path}")
    private String setups;

    private final LocalRESTEventRepository localRESTEventRepository;
    private final ProviderEventRepository providerEventRepository;

    @Autowired
    public SetupService(LocalRESTEventRepository localRESTEventRepository, ProviderEventRepository providerEventRepository) {
        this.localRESTEventRepository = localRESTEventRepository;
        this.providerEventRepository = providerEventRepository;
    }

    public void createSetup(String setupName) throws SeMoDeException {
        this.setupConfig = new SetupConfig(setupName);
        this.fileHandler = new PipelineFileHandler(setupName, this.setups);
        this.fileHandler.createFolderStructure();
    }

    public SetupConfig getSetup(String setupName) throws SeMoDeException {
        this.fileHandler = new PipelineFileHandler(setupName, this.setups);
        this.setupConfig = this.fileHandler.loadUserConfig();
        return this.setupConfig;
    }

    public List<String> getSetupNames() {
        List<String> setupList = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(this.setups))) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    setupList.add(p.getFileName().toString());
                }
            }
        } catch (IOException e) {
            // TODO error handling and show an error with a pop up window
        }
        return setupList;
    }

    public void updateSetup(SetupConfig setupConfig) throws SeMoDeException {
        if (setupConfig != null && setupConfig.isDeployed() && setupConfig.getBenchmarkConfig().benchmarkMode == null) {
            setupConfig.getBenchmarkConfig().setBenchmarkMode(this.setupConfig.getBenchmarkConfig().getBenchmarkMode());
        }
        this.setupConfig = setupConfig;
        this.fileHandler.saveUserConfigToFile(setupConfig);
    }

    // TODO maybe DeploymentService?? handle Exception properly (cleanup)
    public void deployFunctions() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            benchmark.deploy();
            // during deployment a lot of internals are set and therefore the update here is needed
            this.setupConfig.setDeployed(true);
            this.updateSetup(this.setupConfig);
        }
    }

    public void undeployFunctions() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            benchmark.undeploy();
            // during undeployment a lot of  internals are reset, therefore setup config must be stored again
            this.setupConfig.setDeployed(false);
            this.updateSetup(this.setupConfig);
        }
    }

    // TODO Execution service?
    // TODO comment

    /**
     * Creates a list of all benchmark configs, which are enabled. Currently only AWS is enabled.
     */
    private List<BenchmarkMethods> createBenchmarkMethodsFromConfig(final String setupName) throws SeMoDeException {
        final List<BenchmarkMethods> benchmarkMethods = new ArrayList<>();
        // if this is the case, the benchmark config was initialized, see function above
        if (this.setupConfig.getBenchmarkConfig().awsBenchmarkConfig != null) {
            benchmarkMethods.add(new AWSBenchmark(setupName, this.setupConfig.getBenchmarkConfig().awsBenchmarkConfig));
        }
        return benchmarkMethods;
    }

    public void executeBenchmark() throws SeMoDeException {
        this.setupConfig.getBenchmarkConfig().logBenchmarkStartTime();

        final BenchmarkExecutor benchmarkExecutor = new BenchmarkExecutor(this.fileHandler.pathToBenchmarkExecution, this.setupConfig.getBenchmarkConfig());
        benchmarkExecutor.generateLoadPattern();
        List<LocalRESTEvent> events = benchmarkExecutor.executeBenchmark(this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName()));
        this.localRESTEventRepository.saveAll(events);
        log.info("Sucessfully stored " + events.size() + " benchmark execution events!");

        this.setupConfig.getBenchmarkConfig().logBenchmarkEndTime();
        this.updateSetup(this.setupConfig);
    }

    // TODO calibration features

    public void deployCalibration(String platform) throws SeMoDeException {
        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.deployCalibration();
            this.setupConfig.setCalibrationDeployed(true);
            this.updateSetup(this.setupConfig);
        }
    }

    private CalibrationMethods getCalibrationMethod(String platform) throws SeMoDeException {
        CalibrationMethods calibration = null;
        if (CalibrationPlatform.LOCAL.getText().equals(platform)) {
            calibration = new LocalCalibration(this.setupConfig.getSetupName(), this.fileHandler.pathToCalibration, this.setupConfig.getCalibrationConfig().getLocalConfig());
        } else if (CalibrationPlatform.AWS.getText().equals(platform)) {
            calibration = new AWSCalibration(this.setupConfig.getSetupName(), this.setupConfig.getCalibrationConfig().getAwsCalibrationConfig());
        }
        return calibration;
    }

    public void startCalibration(String platform) throws SeMoDeException {
        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.startCalibration();
        }
    }

    // TODO document
    public void fetchPerformanceData() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            int numberOfPerformanceDataMappings = 0;
            log.info("Fetch performance data for " + benchmark.getPlatform());
            List<PerformanceData> data = benchmark.getPerformanceDataFromPlatform(LocalDateTime.parse(this.setupConfig.getBenchmarkConfig().startTime), LocalDateTime.parse(this.setupConfig.getBenchmarkConfig().endTime));
            for (PerformanceData performanceData : data) {
                Optional<ProviderEvent> event = this.providerEventRepository.findByPlatformId(performanceData.getPlatformId());
                if (event.isPresent()) {
                    ProviderEvent e = event.get();
                    if (e.getPerformanceData() == null) {
                        e.setPerformanceData(performanceData);
                        this.providerEventRepository.save(e);
                        numberOfPerformanceDataMappings++;
                    }
                }
            }
            log.info("Successfully stored " + numberOfPerformanceDataMappings + " performance data to local benchmarking events");
        }
    }

    public void computeMapping() throws SeMoDeException {
        new MappingMaster(this.setupConfig.getCalibrationConfig().getMappingCalibrationConfig()).computeMapping();
        this.updateSetup(this.setupConfig);
    }

    public void runFunctionLocally() throws SeMoDeException {
        ContainerExecutor containerExecutor = new ContainerExecutor(this.fileHandler.pathToCalibration, this.setupConfig.getCalibrationConfig().getMappingCalibrationConfig(), this.setupConfig.getCalibrationConfig().getRunningCalibrationConfig());
        containerExecutor.executeLocalProfiles();
    }

    // Old parts...
    @Deprecated
    public void updateAWSConfig(final String region, final String runtime, final String awsArnRole,
                                final String functionHandler, final String timeout, final String deployLinpack, final String targetUrl,
                                final String apiKey, final String bucketName, final String memorySizes, final String numberOfAWSExecutions,
                                final String enabled, final String pathToSource) throws SeMoDeException {
        try {
            this.setupConfig.getCalibrationConfig().getAwsCalibrationConfig().update(region, runtime, awsArnRole, functionHandler, timeout, deployLinpack, targetUrl, apiKey, bucketName, memorySizes, numberOfAWSExecutions, enabled, pathToSource);
        } catch (final IOException e) {
            throw new SeMoDeException("Error during memory Size parsing");
        }
    }

    @Deprecated
    public void updateLocalConfig(final String localSteps, final String numberOfLocalCalibrations,
                                  final String enabled, final String dockerSourceFolder) {
        this.setupConfig.getCalibrationConfig().getLocalConfig().update(localSteps, numberOfLocalCalibrations, enabled, dockerSourceFolder);
    }

    @Deprecated
    public AWSCalibrationConfig getAWSConfig() {
        return this.setupConfig.getCalibrationConfig().getAwsCalibrationConfig();
    }

    @Deprecated
    public LocalCalibrationConfig getLocalConfig() {
        return this.setupConfig.getCalibrationConfig().getLocalConfig();
    }

    @Deprecated
    public boolean isLocalEnabled() {
        return this.setupConfig.getCalibrationConfig().getLocalConfig().isLocalEnabled();
    }

    @Deprecated
    public boolean isAWSEnabled() {
        return true;
    }

    @Deprecated
    public double getLocalSteps() {
        return this.setupConfig.getCalibrationConfig().getLocalConfig().getLocalSteps();
    }

    /**
     * Load the user config from file.
     */
    @Deprecated
    public void loadUserConfig(final String path) throws SeMoDeException {
        final ObjectMapper om = new ObjectMapper();
        try {
            this.setupConfig = om.readValue(Paths.get(path).toFile(), SetupConfig.class);
        } catch (final IOException e) {
            throw new SeMoDeException("Error while parsing the " + path + " file. Check the config.");
        }
    }

    @Deprecated
    public void saveUserConfigToFile(final Path pathToConfig) throws SeMoDeException {
        try {
            new ObjectMapper().writer().withDefaultPrettyPrinter().writeValue(pathToConfig.toFile(),
                    this.setupConfig);
        } catch (final IOException e) {
            throw new SeMoDeException("Configuration could not be saved.", e);
        }
    }

    @Deprecated
    public String getPrintableString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.setupConfig);
    }

    @Deprecated
    public void updateAWSFunctionBenchmarkConfig(final String region, final String runtime,
                                                 final String awsArnRole, final String functionHandler,
                                                 final String timeout, final String memorySizes, final String pathToSource, final String targetUrl,
                                                 final String apiKey) throws SeMoDeException {
        try {
            this.setupConfig.getBenchmarkConfig().getAwsBenchmarkConfig().getFunctionConfig().update(region, runtime, awsArnRole, functionHandler, timeout, targetUrl, apiKey, memorySizes, pathToSource);
        } catch (final IOException e) {
            throw new SeMoDeException("Error during memory Size parsing");
        }
    }

    @Deprecated
    public void updateGlobalBenchmarkParameters(final String numberOfThreads, final String benchmarkingMode,
                                                final String benchmarkingParameters, final String postArgument) {
        this.setupConfig.getBenchmarkConfig().update(numberOfThreads, benchmarkingMode, benchmarkingParameters, postArgument);
    }

    @Deprecated
    public BenchmarkConfig getBenchmarkConfig() {
        return this.setupConfig.getBenchmarkConfig();
    }

    @Deprecated
    public void logBenchmarkStartTime() {
        this.setupConfig.getBenchmarkConfig().logBenchmarkStartTime();
    }

    @Deprecated
    public void logBenchmarkEndTime() {
        this.setupConfig.getBenchmarkConfig().logBenchmarkEndTime();
    }

    @Deprecated
    public Pair<LocalDateTime, LocalDateTime> getStartAndEndTime() throws SeMoDeException {
//        try {
//            return new ImmutablePair<>(this.setupConfig.getBenchmarkConfig().startTime,
//                    this.setupConfig.getBenchmarkConfig().endTime);
//        } catch (final DateTimeParseException e) {
        throw new SeMoDeException("Start or end time not parsable: start: " + this.setupConfig.getBenchmarkConfig().startTime
                + " end: " + this.setupConfig.getBenchmarkConfig().endTime);
//        }
    }

    @Deprecated
    public void updateMappingConfig(final String localCalibrationFile, final String providerCalibrationFile,
                                    final String memoryJSON) throws SeMoDeException {
        this.setupConfig.getCalibrationConfig().getMappingCalibrationConfig().update(localCalibrationFile, providerCalibrationFile, memoryJSON);
    }

    @Deprecated
    public MappingCalibrationConfig getMappingConfig() {
        return this.setupConfig.getCalibrationConfig().getMappingCalibrationConfig();
    }

    @Deprecated
    public void updateRunningConfig(final String dockerSourceFolder, final String environmentVariablesFile,
                                    final String numberOfProfiles) {
        this.setupConfig.getCalibrationConfig().getRunningCalibrationConfig().update(dockerSourceFolder, environmentVariablesFile, numberOfProfiles);
    }

    @Deprecated
    public RunningCalibrationConfig getRunningConfig() {
        return this.setupConfig.getCalibrationConfig().getRunningCalibrationConfig();
    }


}
