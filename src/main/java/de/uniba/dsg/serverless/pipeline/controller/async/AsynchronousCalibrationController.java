package de.uniba.dsg.serverless.pipeline.controller.async;

import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigId;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.service.CalibrationService;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import de.uniba.dsg.serverless.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class AsynchronousCalibrationController {

    @Autowired
    private SetupService service;
    @Autowired
    private CalibrationService calibrationService;

    @GetMapping("semode/v1/{setup}/calibration/mapping")
    public ResponseEntity<String> mapping(@PathVariable(value = "setup") String setupName,
                                          @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            String mappingResult = this.calibrationService.computeMapping(setupName).toString();
            log.info("Mapping: " + mappingResult);
            return ResponseEntity.ok(mappingResult);
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/calibration/version/{version}")
    public ResponseEntity<CalibrationConfig> getCalibrationConfig(@PathVariable(value = "setup") String setupName,
                                                                  @PathVariable(value = "version") Integer version,
                                                                  @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.calibrationService.getCalibrationBySetupAndVersion(setupName, version));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/calibration/{calibrationId}/data")
    public ResponseEntity<IPointDto[]> getCalibrationData(@PathVariable(value = "setup") String setupName,
                                                          @PathVariable(value = "calibrationId") Integer calibrationId,
                                                          @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.calibrationService.getCalibrationDataBySetupAndId(setupName, calibrationId));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/calibration/{calibrationId}/mapping")
    public ResponseEntity<String> getRegressionFunction(@PathVariable(value = "setup") String setupName,
                                                        @PathVariable(value = "calibrationId") Integer calibrationId,
                                                        @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.calibrationService.getRegressionFunction(setupName, calibrationId));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/profiles")
    public ResponseEntity<List<ICalibrationConfigId>> getProfilesForSetup(@PathVariable(value = "setup") String setupName,
                                                                          @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.calibrationService.getProfilesForSetup(setupName));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/profiles/{calibrationConfigId}")
    public ResponseEntity<IPointDto[]> getProfilePointsForSetupAndCalibration(@PathVariable(value = "setup") String setupName,
                                                                              @PathVariable("calibrationConfigId") Integer id,
                                                                              @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.calibrationService.getProfilePointsForSetupAndCalibration(setupName, id).toArray(IPointDto[]::new));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/calibration/start/{platform}")
    public ResponseEntity startCalibration(@PathVariable(value = "setup") String setupName,
                                           @PathVariable("platform") String platform,
                                           @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.calibrationService.startCalibration(setupName, platform);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }

    @GetMapping("semode/v1/{setup}/calibration/deploy/{platform}")
    public ResponseEntity deployCalibration(@PathVariable(value = "setup") String setupName,
                                            @PathVariable("platform") String platform,
                                            @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.calibrationService.deployCalibration(setupName, platform);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/calibration/undeploy/{platform}")
    public ResponseEntity undeployCalibration(@PathVariable(value = "setup") String setupName,
                                              @PathVariable("platform") String platform,
                                              @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.calibrationService.undeployCalibration(setupName, platform);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    @GetMapping("semode/v1/{setup}/simulation/run")
    public ResponseEntity runFunction(@PathVariable(value = "setup") String setupName,
                                      @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.calibrationService.runFunctionLocally(setupName);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
