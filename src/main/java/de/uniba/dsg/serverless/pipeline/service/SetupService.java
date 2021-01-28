package de.uniba.dsg.serverless.pipeline.service;

import de.uniba.dsg.serverless.pipeline.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.AWSBenchmark;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import de.uniba.dsg.serverless.pipeline.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.pipeline.calibration.provider.AWSCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.provider.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.repo.LocalRESTEventRepository;
import de.uniba.dsg.serverless.pipeline.repo.ProviderEventRepository;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
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
        if (setupConfig != null && setupConfig.isDeployed() && setupConfig.getBenchmarkConfig().getBenchmarkMode() == null) {
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
        if (this.setupConfig.getBenchmarkConfig().getAwsBenchmarkConfig() != null) {
            benchmarkMethods.add(new AWSBenchmark(setupName, this.setupConfig.getBenchmarkConfig().getAwsBenchmarkConfig()));
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

    public void undeployCalibration(String platform) throws SeMoDeException {

        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.undeployCalibration();
            this.setupConfig.setCalibrationDeployed(false);
            this.updateSetup(this.setupConfig);
        }
    }

    // TODO document
    public void fetchPerformanceData() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            int numberOfPerformanceDataMappings = 0;
            log.info("Fetch performance data for " + benchmark.getPlatform());
            List<PerformanceData> data = benchmark.getPerformanceDataFromPlatform(LocalDateTime.parse(this.setupConfig.getBenchmarkConfig().getStartTime()), LocalDateTime.parse(this.setupConfig.getBenchmarkConfig().getEndTime()));
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
}
