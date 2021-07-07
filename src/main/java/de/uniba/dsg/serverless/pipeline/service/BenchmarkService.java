package de.uniba.dsg.serverless.pipeline.service;

import de.uniba.dsg.serverless.pipeline.benchmark.BenchmarkExecutor;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.AWSBenchmark;
import de.uniba.dsg.serverless.pipeline.benchmark.methods.BenchmarkMethods;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.repo.BenchmarkConfigRepository;
import de.uniba.dsg.serverless.pipeline.repo.LocalRESTEventRepository;
import de.uniba.dsg.serverless.pipeline.repo.PerformanceDataRepository;
import de.uniba.dsg.serverless.pipeline.repo.ProviderEventRepository;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkVersionAggregate;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.util.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BenchmarkService {


    @Value("${semode.setups.path}")
    private String setups;

    private SetupService setupService;
    private BenchmarkConfigRepository benchmarkConfigRepository;
    private PerformanceDataRepository performanceDataRepository;
    private ProviderEventRepository providerEventRepository;
    private LocalRESTEventRepository localRESTEventRepository;

    @Autowired
    public BenchmarkService(SetupService setupService,
                            BenchmarkConfigRepository benchmarkConfigRepository,
                            PerformanceDataRepository performanceDataRepository,
                            ProviderEventRepository providerEventRepository,
                            LocalRESTEventRepository localRESTEventRepository) {
        this.setupService = setupService;
        this.benchmarkConfigRepository = benchmarkConfigRepository;
        this.performanceDataRepository = performanceDataRepository;
        this.providerEventRepository = providerEventRepository;
        this.localRESTEventRepository = localRESTEventRepository;
    }

    public void saveBenchmark(BenchmarkConfig config, String setupName) throws SeMoDeException {
        SetupConfig setupConfig = this.setupService.getSetup(setupName);
        // benchmark configuration has changed
        BenchmarkConfig currentBenchmarkConfig = setupConfig.getBenchmarkConfig();
        if (currentBenchmarkConfig.equals(config) == false) {
            config.setVersionNumber(currentBenchmarkConfig.getVersionNumber() + 1);
            // set the current benchmark config
            setupConfig.setBenchmarkConfig(config);
            // store setup config and use cascade mechanism to store also the benchmark config
            this.setupService.save(setupConfig);
            log.info("Stored a new benchmark configuration for setup " + setupName + " with version number " + config.getVersionNumber());
        } else {
            log.info("No changes in the benchmark config for setup " + setupName);
        }
    }

    /**
     * <p>Fetches the performance data from the specified version of the benchmark configuration.</p>
     *
     * @param setupName
     * @param version
     * @throws SeMoDeException
     */
    public void fetchPerformanceData(String setupName, Integer version) throws SeMoDeException {
        BenchmarkConfig config = this.benchmarkConfigRepository.findBenchmarkConfigBySetupNameAndVersionNumber(setupName, version);
        if (this.getBenchmarkDataByVersion(setupName, version).length > 1) {
            log.warn("Performance Data is already fetched for setup " + setupName + " and version " + version);
        } else {
            fetchPerformanceData(setupName, config);
        }
    }

    /**
     * <p>Fetches the performance data from the most recent version of the benchmark configuration.</p>
     *
     * @param setup
     * @throws SeMoDeException
     */
    public void fetchPerformanceData(String setup) throws SeMoDeException {
        BenchmarkConfig config = this.benchmarkConfigRepository.findBySetupNameOrderByVersionNumberDesc(setup);
        if (this.getBenchmarkDataByVersion(setup, config.getVersionNumber()).length > 1) {
            log.warn("Performance Data is already fetched for setup " + setup + " and version " + config.getVersionNumber());
        } else {
            fetchPerformanceData(setup, config);
        }
    }

    private void fetchPerformanceData(String setup, BenchmarkConfig config) throws SeMoDeException {
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(setup, config)) {
            int numberOfPerformanceDataMappings = 0;
            int numberOfUnassignedPerformanceData = 0;
            int numberOfAlreadyAssignedPerformanceData = 0;
            List<PerformanceData> data = benchmark.getPerformanceDataFromPlatform(config.getStartTime(), config.getEndTime());
            log.info("Fetch performance data for " + benchmark.getPlatform());
            for (PerformanceData performanceData : data) {
                /*
                 * Add benchmark config to the performance Data to load all data associated with the benchmark
                 * experiments either they have a local rest event (when the timeout of the API gateway was not
                 * exceeded or when doing other experiments which are not related to API gateways.
                 */
                performanceData.setBenchmarkConfig(config);
                Optional<ProviderEvent> event = this.providerEventRepository.findByPlatformId(performanceData.getPlatformId());
                if (event.isPresent()) {
                    ProviderEvent e = event.get();
                    if (e.getPerformanceData() == null) {
                        e.setPerformanceData(performanceData);
                        this.providerEventRepository.save(e);
                        numberOfPerformanceDataMappings++;
                    } else {
                        // could be the case, that an experiment was split and for the UI we combine data,
                        // but the original local rest events etc. should be part of the original benchmark experiment
                        this.performanceDataRepository.save(performanceData);
                        numberOfAlreadyAssignedPerformanceData++;
                    }
                }
                // no matching of provider and performance data possible (due to timeouts etc.)
                else {
                    this.performanceDataRepository.save(performanceData);
                    numberOfUnassignedPerformanceData++;
                }
            }
            log.info("Successfully stored " + numberOfPerformanceDataMappings + " performance data to local benchmarking events");
            log.info("Successfully stored " + numberOfAlreadyAssignedPerformanceData + " already assigned performance data to benchmarking events");
            log.info("Successfully stored " + numberOfUnassignedPerformanceData + " only performance data without local benchmarking events");
        }
    }

    public void deployFunctions(String setup) throws SeMoDeException {
        BenchmarkConfig config = this.benchmarkConfigRepository.findBySetupNameOrderByVersionNumberDesc(setup);
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(setup, config)) {
            benchmark.deploy();

            // during deployment a lot of internals are set and therefore the update here is needed
            config.setDeployed(true);

            this.increaseBenchmarkVersionNumberAndForceNewEntryInDb(config);
        }
    }

    public void undeployFunctions(String setup) throws SeMoDeException {
        BenchmarkConfig config = this.benchmarkConfigRepository.findBySetupNameOrderByVersionNumberDesc(setup);
        for (final BenchmarkMethods benchmark : this.createBenchmarkMethodsFromConfig(setup, config)) {
            benchmark.undeploy();
            // during undeployment a lot of  internals are reset, therefore setup config must be stored again
            config.setDeployed(false);

            this.increaseBenchmarkVersionNumberAndForceNewEntryInDb(config);
        }
    }

    /**
     * Creates a list of all benchmark configs, which are enabled. Currently only AWS is enabled.
     */
    private List<BenchmarkMethods> createBenchmarkMethodsFromConfig(final String setupName, BenchmarkConfig config) throws SeMoDeException {
        final List<BenchmarkMethods> benchmarkMethods = new ArrayList<>();
        // if this is the case, the benchmark config was initialized, see function above
        if (config.getAwsBenchmarkConfig() != null) {
            benchmarkMethods.add(new AWSBenchmark(setupName, config.getAwsBenchmarkConfig()));
        }
        return benchmarkMethods;
    }

    /**
     * Increase the version of the benchmark configuration and force a new entry in the database.
     * Setup config is read from the db and finally used for cascading this change.
     *
     * @param config
     * @throws SeMoDeException
     */
    private void increaseBenchmarkVersionNumberAndForceNewEntryInDb(BenchmarkConfig config) throws SeMoDeException {
        BenchmarkConfig newVersion = config.increaseVersion();
        SetupConfig setupConfig = this.setupService.getSetup(config.getSetupName());
        setupConfig.setBenchmarkConfig(newVersion);
        this.setupService.save(setupConfig);
    }

    public void executeBenchmark(String setup) throws SeMoDeException {
        BenchmarkConfig config = this.benchmarkConfigRepository.findBySetupNameOrderByVersionNumberDesc(setup);
        config.logBenchmarkStartTime();

        PipelineFileHandler fileHandler = new PipelineFileHandler(setup, this.setups);
        final BenchmarkExecutor benchmarkExecutor = new BenchmarkExecutor(fileHandler.pathToBenchmarkExecution, config);
        benchmarkExecutor.generateLoadPattern();
        List<LocalRESTEvent> events = benchmarkExecutor.executeBenchmark(this.createBenchmarkMethodsFromConfig(setup, config));

        config.logBenchmarkEndTime();

        this.increaseBenchmarkVersionNumberAndForceNewEntryInDb(config);

        // set the relationship before storing the rest event
        events.stream().forEach(localRESTEvent -> localRESTEvent.setBenchmarkConfig(config));
        this.localRESTEventRepository.saveAll(events);
        log.info("Sucessfully stored " + events.size() + " benchmark execution events!");
    }

    public BenchmarkConfig getBenchmarkConfigBySetupAndVersion(String setup, Integer version) {
        return this.benchmarkConfigRepository.findBenchmarkConfigBySetupNameAndVersionNumber(setup, version);
    }

    public BenchmarkConfig getCurrentBenchmarkForSetup(String setup) {
        return this.benchmarkConfigRepository.findBySetupNameOrderByVersionNumberDesc(setup);
    }

    public void changePublicVisiblityPropertyForBenchmarkVersion(String setupName, Integer version) {

        BenchmarkConfig config = this.getBenchmarkConfigBySetupAndVersion(setupName, version);
        config.setVersionVisible(!config.isVersionVisible());
        log.info("Change public visibility property for setup " + setupName + " and benchmark version " + version + " to: " + config.isVersionVisible());
        this.benchmarkConfigRepository.save(config);

    }

    public void changeDescriptionForBenchmarkVersion(String setupName, int version, String newDescription) {
        BenchmarkConfig config = this.getBenchmarkConfigBySetupAndVersion(setupName, version);
        config.setDescription(newDescription);
        log.info("Change description for setup " + setupName + " and benchmark version " + version + " to: " + newDescription);
        this.benchmarkConfigRepository.save(config);
    }

    public IPointDto[] getBenchmarkDataByVersion(String setupName, Integer version) {
        return this.benchmarkConfigRepository.getBenchmarkExecutionPointsProviderView(setupName, version).toArray(IPointDto[]::new);
    }

    public List<IBenchmarkVersionAggregate> getBenchmarkVersions(String setupName) {
        return this.benchmarkConfigRepository.countEventsByGroupingThemOnTheirVersionNumber(setupName);
    }
}
