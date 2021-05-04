package de.uniba.dsg.serverless.pipeline.controller;

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

    // TODO include setupName here
    @GetMapping("{setup}/calibration/mapping")
    public ResponseEntity<String> mapping(@PathVariable(value = "setup") String setupName) throws SeMoDeException {
        String mappingResult = this.service.computeMapping().toString();
        log.info("Mapping: " + mappingResult);
        return ResponseEntity.ok(mappingResult);
    }

    @GetMapping("{setup}/calibration/version/{version}")
    public CalibrationConfig getCalibrationConfig(@PathVariable(value = "setup") String setup, @PathVariable(value = "version") Integer version) {
        return this.service.getCalibrationBySetupAndVersion(setup, version);
    }

    @GetMapping("{setup}/calibration/{calibrationId}/data")
    public IPointDto[] getCalibrationData(@PathVariable(value = "setup") String setup, @PathVariable(value = "calibrationId") Integer calibrationId) {
        return this.service.getCalibrationDataBySetupAndId(setup, calibrationId);
    }

    @GetMapping("{setup}/profiles")
    public List<ICalibrationConfigId> getProfilesForSetup(@PathVariable(value = "setup") String setup) {
        return this.service.getProfilesForSetup(setup);
    }

    @GetMapping("{setup}/profiles/{calibrationConfigId}")
    public IPointDto[] getProfilePointsForSetupAndCalibration(@PathVariable(value = "setup") String setup, @PathVariable("calibrationConfigId") Integer id) {
        return this.service.getProfilePointsForSetupAndCalibration(setup, id).toArray(IPointDto[]::new);
    }
}
