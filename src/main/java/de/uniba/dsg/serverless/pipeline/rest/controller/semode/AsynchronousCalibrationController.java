package de.uniba.dsg.serverless.pipeline.rest.controller.semode;

import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigId;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class AsynchronousCalibrationController {

    @Autowired
    private SetupService service;

    @GetMapping("semode/v1/{setup}/calibration/mapping")
    public ResponseEntity<String> mapping(@PathVariable(value = "setup") String setupName) throws SeMoDeException {
        String mappingResult = this.service.computeMapping(setupName).toString();
        log.info("Mapping: " + mappingResult);
        return ResponseEntity.ok(mappingResult);
    }

    @GetMapping("semode/v1/{setup}/calibration/version/{version}")
    public CalibrationConfig getCalibrationConfig(@PathVariable(value = "setup") String setup, @PathVariable(value = "version") Integer version) {
        return this.service.getCalibrationBySetupAndVersion(setup, version);
    }

    @GetMapping("semode/v1/{setup}/calibration/{calibrationId}/data")
    public IPointDto[] getCalibrationData(@PathVariable(value = "setup") String setup, @PathVariable(value = "calibrationId") Integer calibrationId) {
        return this.service.getCalibrationDataBySetupAndId(setup, calibrationId);
    }

    @GetMapping("semode/v1/{setup}/profiles")
    public List<ICalibrationConfigId> getProfilesForSetup(@PathVariable(value = "setup") String setup) {
        return this.service.getProfilesForSetup(setup);
    }

    @GetMapping("semode/v1/{setup}/profiles/{calibrationConfigId}")
    public IPointDto[] getProfilePointsForSetupAndCalibration(@PathVariable(value = "setup") String setup, @PathVariable("calibrationConfigId") Integer id) {
        return this.service.getProfilePointsForSetupAndCalibration(setup, id).toArray(IPointDto[]::new);
    }

    @GetMapping("semode/v1/{setup}/calibration/start/{platform}")
    public ResponseEntity startCalibration(@PathVariable(value = "setup") String setup, @PathVariable("platform") String platform) throws SeMoDeException {
        this.service.startCalibration(setup, platform);
        return ResponseEntity.ok().build();
    }

    @GetMapping("semode/v1/{setup}/calibration/deploy/{platform}")
    public ResponseEntity deployCalibration(@PathVariable(value = "setup") String setup, @PathVariable("platform") String platform) throws SeMoDeException {
        this.service.deployCalibration(setup, platform);
        return ResponseEntity.ok().build();
    }

    @GetMapping("semode/v1/{setup}/calibration/undeploy/{platform}")
    public ResponseEntity undeployCalibration(@PathVariable(value = "setup") String setup, @PathVariable("platform") String platform) throws SeMoDeException {
        this.service.undeployCalibration(setup, platform);
        return ResponseEntity.ok().build();
    }


    @GetMapping("semode/v1/{setup}/simulation/run")
    public ResponseEntity runFunction(@PathVariable(value = "setup") String setup) throws SeMoDeException {
        this.service.runFunctionLocally(setup);
        return ResponseEntity.ok().build();
    }
}
