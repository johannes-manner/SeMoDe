package de.uniba.dsg.serverless.pipeline.service;

import de.uniba.dsg.serverless.pipeline.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ProfileRecord;
import de.uniba.dsg.serverless.pipeline.calibration.provider.AWSCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.provider.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.repo.*;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigEventAggregate;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigId;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.util.ConversionUtils;
import de.uniba.dsg.serverless.pipeline.util.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import de.uniba.dsg.serverless.users.User;
import de.uniba.dsg.serverless.users.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wrapper to change the attributes of the user config class. If the changes are made directly in the model classes,
 * there occur json parsing errors.
 */
@Slf4j
@Service
@SessionScope
public class SetupService {

    @Value("${semode.setups.path}")
    private String setups;

    // model
    private SetupConfig setupConfig;
    private PipelineFileHandler fileHandler;

    private final SetupConfigRepository setupConfigRepository;
    private final LocalRESTEventRepository localRESTEventRepository;
    private final ProviderEventRepository providerEventRepository;
    private final PerformanceDataRepository performanceDataRepository;
    private final CalibrationEventRepository calibrationEventRepository;
    private final BenchmarkConfigRepository benchmarkConfigRepository;
    private final CalibrationConfigRepository calibrationConfigRepository;
    private final ProfileRecordRepository profileRecordRepository;
    private final UserRepository userRepository;

    private final ConversionUtils conversionUtils;

    @Autowired
    public SetupService(SetupConfigRepository setupConfigRepository,
                        LocalRESTEventRepository localRESTEventRepository,
                        ProviderEventRepository providerEventRepository,
                        PerformanceDataRepository performanceDataRepository,
                        CalibrationEventRepository calibrationEventRepository,
                        BenchmarkConfigRepository benchmarkConfigRepository,
                        CalibrationConfigRepository calibrationConfigRepository,
                        ProfileRecordRepository profileRecordRepository,
                        UserRepository userRepository,
                        ConversionUtils conversionUtils) {
        this.setupConfigRepository = setupConfigRepository;
        this.localRESTEventRepository = localRESTEventRepository;
        this.providerEventRepository = providerEventRepository;
        this.performanceDataRepository = performanceDataRepository;
        this.calibrationEventRepository = calibrationEventRepository;
        this.benchmarkConfigRepository = benchmarkConfigRepository;
        this.calibrationConfigRepository = calibrationConfigRepository;
        this.profileRecordRepository = profileRecordRepository;
        this.userRepository = userRepository;
        this.conversionUtils = conversionUtils;
    }

    // TODO - really local and in DB?
    public void createSetup(String setupName, User owner) throws SeMoDeException {
        this.setupConfig = new SetupConfig(setupName, owner);
        this.fileHandler = new PipelineFileHandler(setupName, this.setups);

        this.setupConfigRepository.save(this.setupConfig);
    }

    // TODO remove
    private void loadSetup(String setupName) throws SeMoDeException {
        Optional<SetupConfig> config = this.setupConfigRepository.findById(setupName);
        if (config.isPresent()) {
            this.setupConfig = config.get();
        } else {
            throw new SeMoDeException("Setup with name " + setupName + " is not present!!");
        }
    }

    /**
     * Returns the setup config by its name, otherwise throws an exception.
     *
     * @param setupName
     * @return
     * @throws SeMoDeException
     */
    public SetupConfig getSetup(String setupName) throws SeMoDeException {
        Optional<SetupConfig> config = this.setupConfigRepository.findById(setupName);
        if (config.isPresent()) {
            return config.get();
        } else {
            throw new SeMoDeException("Setup with name " + setupName + " is not present!!");
        }
    }

    public void save(SetupConfig setupConfig) {
        this.setupConfigRepository.save(setupConfig);
    }

    /**
     * Returns all setups, when the user is admin, otherwise only the setups he is the owner.
     *
     * @param user
     * @return
     */
    public List<SetupConfig> getSetups(User user) {
        if (user.isAdmin()) {
            return this.setupConfigRepository.findAll();
        } else {
            return this.setupConfigRepository.findByOwner(user);
        }
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

    // TODO remove
    private void saveSetup() throws SeMoDeException {
        this.setupConfig = this.setupConfigRepository.save(this.setupConfig);
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

    // TODO calibration features

    public void deployCalibration(String setup, String platform) throws SeMoDeException {
        this.loadSetup(setup);
        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.deployCalibration();
            this.setupConfig.getCalibrationConfig().setDeployed(true);
            this.updateSetup(this.setupConfig);
        }
    }

    // TODO refactor the string here...
    private CalibrationMethods getCalibrationMethod(String platform) throws SeMoDeException {
        CalibrationMethods calibration = null;
        if (CalibrationPlatform.LOCAL.getText().equals(platform)) {
            calibration = new LocalCalibration(this.setupConfig.getSetupName(), this.fileHandler.pathToCalibration, this.setupConfig.getCalibrationConfig().getLocalConfig());
        } else if (CalibrationPlatform.AWS.getText().equals(platform)) {
            calibration = new AWSCalibration(this.setupConfig.getCalibrationConfig().getAwsCalibrationConfig());
        }
        return calibration;
    }

    public void startCalibration(String setup, String platform) throws SeMoDeException {
        this.loadSetup(setup);
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

    public void undeployCalibration(String setup, String platform) throws SeMoDeException {

        this.loadSetup(setup);
        CalibrationMethods calibration = this.getCalibrationMethod(platform);

        if (calibration != null) {
            calibration.undeployCalibration();
            this.setupConfig.getCalibrationConfig().setDeployed(false);
            this.updateSetup(this.setupConfig);
        }
    }

    public Map<Integer, Double> computeMapping(String setup) throws SeMoDeException {
        this.loadSetup(setup);
        MappingCalibrationConfig mappingConfig = this.setupConfig.getCalibrationConfig().getMappingCalibrationConfig();

        // compute mapping
        Map<Integer, Double> memorySizeCPUShare = new MappingMaster().computeMapping(
                mappingConfig.getMemorySizeList(),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getLocalCalibration().getId())),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getProviderCalibration().getId())));
        // return it to the caller
        return memorySizeCPUShare;
    }

    public void runFunctionLocally(String setup) throws SeMoDeException {
        this.loadSetup(setup);
        log.info("Running the function with the computed cpu share for the specified memory settings...");
        Map<Integer, Double> memorySizeCPUShare = this.computeMapping(setup);
        ContainerExecutor containerExecutor = new ContainerExecutor(this.fileHandler.pathToCalibration, memorySizeCPUShare, this.setupConfig.getCalibrationConfig().getRunningCalibrationConfig());
        List<ProfileRecord> records = containerExecutor.executeLocalProfiles();
        log.info("Running the function with the computed cpu share for the specified memory settings successfully terminated!");

        records.stream().forEach(r -> r.setCalibrationConfig(this.setupConfig.getCalibrationConfig()));
        this.profileRecordRepository.saveAll(records);

        log.info("Sucessfully stored " + records.size() + " profiling records to the db");
    }


    /**
     * Returns a Map, where the ID of the calibration and the memory sizes are included.
     *
     * @param setupName
     * @param platform
     * @return
     */
    private Map<Long, String> getCalibrations(String setupName, CalibrationPlatform platform) {
        List<ICalibrationConfigEventAggregate> calibrationEvents = this.calibrationConfigRepository.findCalibrationEventsBySetupName(setupName);
        Map<Long, String> configInformation = new HashMap<>();
        calibrationEvents.stream().filter(a -> a.getPlatform().equals(platform)).forEach(a -> {
            configInformation.put(a.getId(), "Calibration Config Version: " + a.getVersionNumber());
        });
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


    public CalibrationConfig getCalibrationBySetupAndVersion(String setup, Integer version) {
        return this.calibrationConfigRepository.findCalibrationConfigBySetupNameAndVersionNumber(setup, version);
    }

    public IPointDto[] getCalibrationDataBySetupAndId(String setup, Integer calibrationId) {
        return this.calibrationEventRepository.getCalibrationPoints(calibrationId, setup).toArray(IPointDto[]::new);
    }

    public List<ICalibrationConfigId> getProfilesForSetup(String setup) {
        return this.calibrationConfigRepository.getCalibrationConfigIdsWithAssociatedProfiles(setup);
    }

    public List<IPointDto> getProfilePointsForSetupAndCalibration(String setup, Integer id) {
        return this.calibrationConfigRepository.getProfilePointsBySetupAndCalibrationId(setup, id);
    }

    /**
     * Checks, if the user is also the owner of the setup or if he is an admin.
     * In both cases, he can access the setup otherwise he gets an error page (FORBIDDEN).
     *
     * @param setupName
     * @param user
     * @return
     */
    public boolean checkSetupAccessRights(String setupName, User user) {
        SetupConfig config = this.setupConfigRepository.findBySetupName(setupName);
        if (config != null && config.getOwner() != null && config.getOwner().equals(user)) {
            return true;
        }
        return user.isAdmin();
    }


}
