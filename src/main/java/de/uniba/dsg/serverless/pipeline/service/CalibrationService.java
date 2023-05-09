package de.uniba.dsg.serverless.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.uniba.dsg.serverless.pipeline.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.RegressionComputation;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.pipeline.calibration.profiling.ProfileRecord;
import de.uniba.dsg.serverless.pipeline.calibration.provider.AWSCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.provider.CalibrationMethods;
import de.uniba.dsg.serverless.pipeline.calibration.provider.OpenFaasCalibration;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.model.openfaas.OpenFaasStackModel;
import de.uniba.dsg.serverless.pipeline.repo.CalibrationConfigRepository;
import de.uniba.dsg.serverless.pipeline.repo.CalibrationEventRepository;
import de.uniba.dsg.serverless.pipeline.repo.ProfileRecordRepository;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigEventAggregate;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigId;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.util.ConversionUtils;
import de.uniba.dsg.serverless.pipeline.util.PipelineFileHandler;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CalibrationService {

    @Value("${semode.setups.path}")
    private String setups;

    private final OpenFaasStackModel openFaasStackModel;

    private final SetupService setupService;

    private final CalibrationConfigRepository calibrationConfigRepository;
    private final CalibrationEventRepository calibrationEventRepository;
    private final ProfileRecordRepository profileRecordRepository;

    private final ConversionUtils conversionUtils;

    @Autowired
    public CalibrationService(OpenFaasStackModel openFaasStackModel,
                              SetupService setupService,
                              CalibrationConfigRepository calibrationConfigRepository,
                              CalibrationEventRepository calibrationEventRepository,
                              ProfileRecordRepository profileRecordRepository,
                              ConversionUtils conversionUtils) {
        this.openFaasStackModel = openFaasStackModel;
        this.setupService = setupService;
        this.calibrationConfigRepository = calibrationConfigRepository;
        this.calibrationEventRepository = calibrationEventRepository;
        this.profileRecordRepository = profileRecordRepository;
        this.conversionUtils = conversionUtils;
    }

    public CalibrationConfig getCurrentCalibrationConfig(String setupName) throws SeMoDeException {
        return calibrationConfigRepository.getCalibrationConfigBySetupName(setupName);
    }

    public void saveCalibration(CalibrationConfig config, String setupName) throws SeMoDeException {
        SetupConfig setupConfig = this.setupService.getSetup(setupName);
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
            this.setupService.save(setupConfig);
            log.info("Successfully saved new calibration for setup " + setupName);
        }
    }

    public void deployCalibration(String setup, String platform) throws SeMoDeException {
        CalibrationMethods calibration = this.getCalibrationMethod(setup, platform);

        if (calibration != null) {
            calibration.deployCalibration();
            CalibrationConfig calibrationConfig = this.getCurrentCalibrationConfig(setup);
            calibrationConfig.setDeployed(true);
            this.calibrationConfigRepository.save(calibrationConfig);
            log.info("Successfully deployed calibration on platform " + platform + " for setup " + setup);
        }
    }

    private CalibrationMethods getCalibrationMethod(String setup, String platform) throws SeMoDeException {
        CalibrationMethods calibration = null;
        if (CalibrationPlatform.LOCAL.getText().equals(platform)) {
            PipelineFileHandler fileHandler = new PipelineFileHandler(setup, this.setups);
            calibration = new LocalCalibration(setup, fileHandler.pathToCalibration, this.getCurrentCalibrationConfig(setup).getLocalConfig());
        } else if (CalibrationPlatform.AWS.getText().equals(platform)) {
            calibration = new AWSCalibration(this.getCurrentCalibrationConfig(setup).getAwsCalibrationConfig());
        } else if (CalibrationPlatform.OPEN_FAAS.getText().equals(platform)) {
            calibration = new OpenFaasCalibration(this.getCurrentCalibrationConfig(setup).getOpenFaasConfig());
        }
        return calibration;
    }

    public void startCalibration(String setup, String platform) throws SeMoDeException {

        CalibrationMethods calibration = this.getCalibrationMethod(setup, platform);

        if (calibration != null) {

            CalibrationConfig newCalibrationConfig = this.increaseCalibrationVersionNumberAndForceNewEntry(setup);

            List<CalibrationEvent> events = calibration.startCalibration();
            events.forEach(e -> e.setConfig(newCalibrationConfig));
            this.calibrationEventRepository.saveAll(events);
            log.info("Sucessfully stored " + events.size() + " calibration events for platform " + platform);
        }
    }

    private CalibrationConfig increaseCalibrationVersionNumberAndForceNewEntry(String setup) throws SeMoDeException {
        CalibrationConfig calibrationConfig = this.getCurrentCalibrationConfig(setup);
        CalibrationConfig newCalibrationConfig = calibrationConfig.increaseVersion();
        SetupConfig setupConfig = this.setupService.getSetup(setup);
        setupConfig.setCalibrationConfig(newCalibrationConfig);
        return setupService.save(setupConfig).getCalibrationConfig();
    }

    public void undeployCalibration(String setup, String platform) throws SeMoDeException {
        CalibrationMethods calibration = this.getCalibrationMethod(setup, platform);

        if (calibration != null) {
            calibration.undeployCalibration();
            CalibrationConfig calibrationConfig = this.getCurrentCalibrationConfig(setup);
            calibrationConfig.setDeployed(false);
            this.calibrationConfigRepository.save(calibrationConfig);
            log.info("Successfully undeployed calibration on platform " + platform + " for setup " + setup);
        }
    }

    public Map<Integer, Double> computeMapping(String setup) throws SeMoDeException {
        MappingCalibrationConfig mappingConfig = this.getCurrentCalibrationConfig(setup).getMappingCalibrationConfig();

        // compute mapping
        Map<Integer, Double> memorySizeCPUShare = new MappingMaster().computeMapping(
                mappingConfig.getMemorySizeList(),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getLocalCalibration().getId())),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getProviderCalibration().getId())));
        // return it to the caller
        return memorySizeCPUShare;
    }

    public Map<Integer, Integer> computeCPUMemoryEquivalents(Long calibrationId) {
        Optional<CalibrationConfig> optionalCalibrationConfig = this.calibrationConfigRepository.findById(calibrationId);
        if (optionalCalibrationConfig.isEmpty()) {
            return new HashMap<>();
        }

        CalibrationConfig calibrationConfig = optionalCalibrationConfig.get();
        MappingCalibrationConfig mappingConfig = calibrationConfig.getMappingCalibrationConfig();

        // compute mapping
        return new MappingMaster().computeCPUMemoryEquivalents(
                calibrationConfig.getMachineConfig().getNoCPUs(),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getLocalCalibration().getId())),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getProviderCalibration().getId())));
    }

    public void runFunctionLocally(String setup) throws SeMoDeException {
        log.info("Running the function with the computed cpu share for the specified memory settings...");

        Map<Integer, Double> memorySizeCPUShare = this.computeMapping(setup);
        PipelineFileHandler fileHandler = new PipelineFileHandler(setup, this.setups);
        CalibrationConfig calibrationConfig = this.getCurrentCalibrationConfig(setup);
        ContainerExecutor containerExecutor = new ContainerExecutor(fileHandler.pathToCalibration, memorySizeCPUShare, calibrationConfig.getRunningCalibrationConfig());
        List<ProfileRecord> records = containerExecutor.executeLocalProfiles();

        log.info("Running the function with the computed cpu share for the specified memory settings successfully terminated!");

        records.stream().forEach(r -> r.setCalibrationConfig(calibrationConfig));
        this.profileRecordRepository.saveAll(records);

        log.info("Sucessfully stored " + records.size() + " profiling records to the db");
    }


    /**
     * Returns a Map, where the ID of the calibration and the memory sizes are included.
     *
     * @param setupName
     * @param platforms
     * @return
     */
    private Map<Long, String> getCalibrations(String setupName, List<CalibrationPlatform> platforms) {
        List<ICalibrationConfigEventAggregate> calibrationEvents = this.calibrationConfigRepository.findCalibrationEventsBySetupName(setupName);
        Map<Long, String> configInformation = new HashMap<>();
        calibrationEvents.stream().filter(a -> platforms.contains(a.getPlatform())).forEach(a -> {
            configInformation.put(a.getId(), "Calibration Config Version: " + a.getVersionNumber() + " on " + a.getPlatform().getText());
        });
        return configInformation;
    }


    public Map<Long, String> getLocalCalibrations(String setupName) {
        return this.getCalibrations(setupName, List.of(CalibrationPlatform.LOCAL));
    }

    /**
     * Currently only AWS is supported
     *
     * @param setupName current available setup, where the data is stored in the db
     * @return information about the available calibration detail
     */
    public Map<Long, String> getProviderCalibrations(String setupName) {
        return this.getCalibrations(setupName, List.of(CalibrationPlatform.AWS, CalibrationPlatform.OPEN_FAAS));
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

    public String generateStackYmlForOpenFaas(String setupName, Integer version) throws SeMoDeException {
        List<String> stackYml = new ArrayList<>();

        CalibrationConfig calibrationConfig = this.calibrationConfigRepository.findCalibrationConfigBySetupNameAndVersionNumber(setupName, version);
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        OpenFaasStackModel model = openFaasStackModel.createFunctions(calibrationConfig.getOpenFaasConfig());

        try {
            return objectMapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new SeMoDeException("Could not generate stack YML", e);
        }
    }

    public String getRegressionFunction(String setupName, Integer calibrationId) {
        List<IPointDto> points = this.calibrationEventRepository.getCalibrationPoints(calibrationId, setupName);
        Map<Double, List<Double>> quotaGflops = new HashMap<>();
        for (IPointDto p : points) {
            if (quotaGflops.containsKey(p.getX()) == false) {
                quotaGflops.put(p.getX(), new ArrayList<>());
            }
            quotaGflops.get(p.getX()).add(p.getY());
        }
        return RegressionComputation.computeRegression(quotaGflops).toString();
    }

    /**
     * Compute the resource settings for the currently conigured provider platform based on the gflops list.
     *
     * @param setupName
     * @return
     */
    // TOT
    public List<Integer> computeGflopsMapping(String setupName, String gflops) throws SeMoDeException {
        MappingCalibrationConfig mappingConfig = this.getCurrentCalibrationConfig(setupName).getMappingCalibrationConfig();

        // compute mapping
        Map<Double, Integer> gflopMapping = new MappingMaster().computeGflopMapping(
                this.getGlopsList(gflops),
                this.conversionUtils.mapCalibrationEventList(this.calibrationEventRepository.findByConfigId(mappingConfig.getProviderCalibration().getId())),
                this.calibrationEventRepository.findFirstPlatformByConfigId(mappingConfig.getProviderCalibration().getId()).getPlatform());
        // return it to the caller
        log.info("Gflops,Resource setting map: " + gflopMapping);
        return gflopMapping.values().stream().collect(Collectors.toList());
    }

    private List<Double> getGlopsList(String gflops) {
        if (gflops == null) {
            return List.of();
        }
        return Arrays.stream(gflops.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }
}
