package de.uniba.dsg.serverless.pipeline.service;

import de.uniba.dsg.serverless.pipeline.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.AWSBenchmark;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import de.uniba.dsg.serverless.pipeline.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ProfileRecord;
import de.uniba.dsg.serverless.pipeline.calibration.provider.AWSCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.provider.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.repo.*;
import de.uniba.dsg.serverless.pipeline.util.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    private final SetupConfigRepository setupConfigRepository;
    private final LocalRESTEventRepository localRESTEventRepository;
    private final ProviderEventRepository providerEventRepository;
    private final CalibrationEventRepository calibrationEventRepository;
    private final BenchmarkConfigRepository benchmarkConfigRepository;
    private final CalibrationConfigRepository calibrationConfigRepository;
    private final ProfileRecordRepository profileRecordRepository;

    @Autowired
    public SetupService(SetupConfigRepository setupConfigRepository,
                        LocalRESTEventRepository localRESTEventRepository,
                        ProviderEventRepository providerEventRepository,
                        CalibrationEventRepository calibrationEventRepository,
                        BenchmarkConfigRepository benchmarkConfigRepository,
                        CalibrationConfigRepository calibrationConfigRepository,
                        ProfileRecordRepository profileRecordRepository) {
        this.setupConfigRepository = setupConfigRepository;
        this.localRESTEventRepository = localRESTEventRepository;
        this.providerEventRepository = providerEventRepository;
        this.calibrationEventRepository = calibrationEventRepository;
        this.benchmarkConfigRepository = benchmarkConfigRepository;
        this.calibrationConfigRepository = calibrationConfigRepository;
        this.profileRecordRepository = profileRecordRepository;
    }

    // TODO - really local and in DB?
    public void createSetup(String setupName) throws SeMoDeException {
        this.setupConfig = new SetupConfig(setupName);
        this.fileHandler = new PipelineFileHandler(setupName, this.setups);

        this.setupConfigRepository.save(this.setupConfig);
    }

    // TODO document only source of truth is the DB
    public SetupConfig getSetup(String setupName) throws SeMoDeException {
        this.fileHandler = new PipelineFileHandler(setupName, this.setups);
        Optional<SetupConfig> config = this.setupConfigRepository.findById(setupName);
        if (config.isPresent()) {
            this.setupConfig = config.get();
            return this.setupConfig;
        } else {
            throw new SeMoDeException("Setup with name " + setupName + " is not present!!");
        }
    }

    public List<String> getSetupNames() {
        return this.setupConfigRepository.findAll().stream().map(SetupConfig::getSetupName).collect(Collectors.toList());
    }

    // TODO remove due to special pages
    public void updateSetup(SetupConfig setupConfig) throws SeMoDeException {
        // Otherwise loose this information when submitting twice the web page
        if (setupConfig != null && setupConfig.getBenchmarkConfig().isDeployed() && setupConfig.getBenchmarkConfig().getBenchmarkMode() == null) {
            setupConfig.getBenchmarkConfig().setBenchmarkMode(this.setupConfig.getBenchmarkConfig().getBenchmarkMode());
        }
        this.setupConfig = setupConfig;
        this.saveSetup();

    }

    private void saveSetup() throws SeMoDeException {
        this.setupConfig = this.setupConfigRepository.save(this.setupConfig);
        this.fileHandler.saveUserConfigToFile(this.setupConfig);
    }

    /*
     * Benchmark Configuration Handling
     */

    public BenchmarkConfig getCurrentBenchmark(String setupName) throws SeMoDeException {
        return this.getSetup(setupName).getBenchmarkConfig();
    }

    private void increaseBenchmarkVersionNumberAndForceNewEntryInDb() throws SeMoDeException {
        this.setupConfig.getBenchmarkConfig().increaseVersion();
        this.saveSetup();
    }

    public void saveBenchmark(BenchmarkConfig config, String setupName) throws SeMoDeException {
        SetupConfig setupConfig = this.getSetup(setupName);
        // benchmark configuration has changed
        BenchmarkConfig currentBenchmarkConfig = setupConfig.getBenchmarkConfig();
        if (currentBenchmarkConfig.equals(config) == false) {
            config.setVersionNumber(currentBenchmarkConfig.getVersionNumber() + 1);
            // set the current benchmark config
            setupConfig.setBenchmarkConfig(config);
            // store setup config and use cascade mechanism to store also the benchmark config
            this.saveSetup();
            log.info("Stored a new benchmark configuration for setup " + setupName + " with version number " + config.getVersionNumber());
        } else {
            log.info("No changes in the benchmark config for setup " + setupName);
        }
    }

    /*
     * Calibration Configuration Handling
     */
    public CalibrationConfig getCurrentCalibrationConfig(String setupName) throws SeMoDeException {
        return this.getSetup(setupName).getCalibrationConfig();
    }

    public void saveCalibration(CalibrationConfig config, String setupName) throws SeMoDeException {
        SetupConfig setupConfig = this.getSetup(setupName);
        CalibrationConfig currentConfig = setupConfig.getCalibrationConfig();

        // update associated local and provider calibration
        MappingCalibrationConfig mapppingConfig = config.getMappingCalibrationConfig();
        if (mapppingConfig.getLocalCalibrationId() != 0) {
            mapppingConfig.setLocalCalibration(this.calibrationConfigRepository.findById(mapppingConfig.getLocalCalibrationId()).get());
        } else {
            mapppingConfig.setLocalCalibration(currentConfig.getMappingCalibrationConfig().getLocalCalibration());
        }
        if (mapppingConfig.getProviderCalibrationId() != 0) {
            mapppingConfig.setProviderCalibration(this.calibrationConfigRepository.findById(mapppingConfig.getProviderCalibrationId()).get());
        } else {
            mapppingConfig.setProviderCalibration(currentConfig.getMappingCalibrationConfig().getProviderCalibration());
        }


        if (currentConfig.equals(config) == false) {
            config.setVersionNumber(currentConfig.getVersionNumber() + 1);
            // set the current configuration config
            setupConfig.setCalibrationConfig(config);
            // store setup config and use cascade mechanism to store also the calibration config
            this.saveSetup();
        }
    }


    // TODO maybe DeploymentService?? handle Exception properly (cleanup)
    public void deployFunctions() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            benchmark.deploy();

            // during deployment a lot of internals are set and therefore the update here is needed
            this.setupConfig.getBenchmarkConfig().setDeployed(true);

            this.increaseBenchmarkVersionNumberAndForceNewEntryInDb();
        }
    }

    public void undeployFunctions() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            benchmark.undeploy();
            // during undeployment a lot of  internals are reset, therefore setup config must be stored again
            this.setupConfig.getBenchmarkConfig().setDeployed(false);

            this.increaseBenchmarkVersionNumberAndForceNewEntryInDb();
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

        this.setupConfig.getBenchmarkConfig().logBenchmarkEndTime();

        this.increaseBenchmarkVersionNumberAndForceNewEntryInDb();

        // set the relationship before storing the rest event
        events.stream().forEach(localRESTEvent -> localRESTEvent.setBenchmarkConfig(this.setupConfig.getBenchmarkConfig()));
        this.localRESTEventRepository.saveAll(events);
        log.info("Sucessfully stored " + events.size() + " benchmark execution events!");
    }

    // TODO calibration features

    public void deployCalibration(String platform) throws SeMoDeException {
        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.deployCalibration();
            this.setupConfig.getCalibrationConfig().setDeployed(true);
            this.updateSetup(this.setupConfig);
        }
    }

    private CalibrationMethods getCalibrationMethod(String platform) throws SeMoDeException {
        CalibrationMethods calibration = null;
        if (CalibrationPlatform.LOCAL.getText().equals(platform)) {
            calibration = new LocalCalibration(this.setupConfig.getSetupName(), this.fileHandler.pathToCalibration, this.setupConfig.getCalibrationConfig().getLocalConfig());
        } else if (CalibrationPlatform.AWS.getText().equals(platform)) {
            calibration = new AWSCalibration(this.setupConfig.getCalibrationConfig().getAwsCalibrationConfig());
        }
        return calibration;
    }

    public void startCalibration(String platform) throws SeMoDeException {
        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {

            this.increaseCalibrationVersionNumberAndForceNewEntry();

            List<CalibrationEvent> events = calibration.startCalibration();
            events.forEach(e -> e.setConfig(this.setupConfig.getCalibrationConfig()));
            this.calibrationEventRepository.saveAll(events);
            log.info("Sucessfully stored " + events.size() + " calibration events for platform " + platform);
        }
    }

    private void increaseCalibrationVersionNumberAndForceNewEntry() throws SeMoDeException {
        this.setupConfig.getCalibrationConfig().increaseVersion();
        this.saveSetup();
    }

    public void undeployCalibration(String platform) throws SeMoDeException {

        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.undeployCalibration();
            this.setupConfig.getCalibrationConfig().setDeployed(false);
            this.updateSetup(this.setupConfig);
        }
    }

    // TODO document
    public void fetchPerformanceData() throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(this.setupConfig.getSetupName())) {
            int numberOfPerformanceDataMappings = 0;
            log.info("Fetch performance data for " + benchmark.getPlatform());
            List<PerformanceData> data = benchmark.getPerformanceDataFromPlatform(this.setupConfig.getBenchmarkConfig().getStartTime(), this.setupConfig.getBenchmarkConfig().getEndTime());
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

    private Map<Double, List<Double>> mapCalibrationEventList(List<CalibrationEvent> calibrationEvents) {
        Map<Double, List<Double>> memoryOrCPUAndItsMeasures = new HashMap<>();
        for (CalibrationEvent event : calibrationEvents) {
            if (!memoryOrCPUAndItsMeasures.containsKey(event.getCpuOrMemoryQuota())) {
                memoryOrCPUAndItsMeasures.put(event.getCpuOrMemoryQuota(), new ArrayList<>());
            }
            memoryOrCPUAndItsMeasures.get(event.getCpuOrMemoryQuota()).add(event.getGflops());
        }
        return memoryOrCPUAndItsMeasures;
    }

    public Map<Integer, Double> computeMapping() throws SeMoDeException {
        MappingCalibrationConfig mappingConfig = this.setupConfig.getCalibrationConfig().getMappingCalibrationConfig();

        // compute mapping
        Map<Integer, Double> memorySizeCPUShare = new MappingMaster().computeMapping(
                mappingConfig.getMemorySizeList(),
                this.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getLocalCalibration().getId())),
                this.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getProviderCalibration().getId())));
        // return it to the caller
        return memorySizeCPUShare;
    }

    public void runFunctionLocally() throws SeMoDeException {
        log.info("Running the function with the computed cpu share for the specified memory settings...");
        Map<Integer, Double> memorySizeCPUShare = this.computeMapping();
        ContainerExecutor containerExecutor = new ContainerExecutor(this.fileHandler.pathToCalibration, memorySizeCPUShare, this.setupConfig.getCalibrationConfig().getRunningCalibrationConfig());
        List<ProfileRecord> records = containerExecutor.executeLocalProfiles();
        log.info("Running the function with the computed cpu share for the specified memory settings successfully terminated!");

        records.stream().forEach(r -> r.setCalibrationConfig(this.setupConfig.getCalibrationConfig()));
        this.profileRecordRepository.saveAll(records);

        log.info("Sucessfully stored " + records.size() + " profiling records to the db");
    }


    public List<BenchmarkConfig> getBenchmarkVersions(String setupName) {
        List<BenchmarkConfig> configs = this.benchmarkConfigRepository.findBySetupNameOrderByVersionNumberDesc(setupName);
        configs.forEach(c -> c.setNumberOfLocalDbEvents(c.getLocalExecutionEvents().size()));
        configs.forEach(
                c -> c.setNumberOfFetchedData(
                        c.getLocalExecutionEvents().stream().filter(LocalRESTEvent::dataAlreadyFetched).mapToInt(e -> 1).sum()
                ));
        return configs;
    }

    private Map<Long, String> getCalibrations(String setupName, CalibrationPlatform platform) {
        List<CalibrationConfig> configList = this.calibrationConfigRepository.findDistinctCalibrationConfigBySetupName(setupName);
        Map<Long, String> configInformation = new HashMap<>();
        for (CalibrationConfig config : configList) {
            String info = config.getCalibrationEvents().stream()
                    .filter(e -> e.getPlatform().equals(platform))
                    .map(CalibrationEvent::getCpuOrMemoryQuota)
                    .distinct()
                    .sorted()
                    .map(d -> d.toString())
                    .collect(Collectors.joining(","));
            if (!info.isBlank()) {
                configInformation.put(config.getId(), platform.getText() + " - " + info);
            }
        }
        return configInformation;
    }

    public Map<Long, String> getLocalCalibrations(String setupName) {
        return this.getCalibrations(setupName, CalibrationPlatform.LOCAL);
    }

    /**
     * Currently only AWS is supported
     *
     * @param setupName current available setup, where the data is stored in the db
     * @return information about the available calibration detail
     */
    public Map<Long, String> getProviderCalibrations(String setupName) {
        return this.getCalibrations(setupName, CalibrationPlatform.AWS);
    }
}
