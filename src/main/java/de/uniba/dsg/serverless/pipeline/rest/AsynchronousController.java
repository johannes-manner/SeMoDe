package de.uniba.dsg.serverless.pipeline.rest;

import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


// TODO refactor this controller!!!
@Slf4j
@RestController
public class AsynchronousController {

    @Autowired
    private SetupService service;


    @GetMapping("/calibration/start/{platform}")
    public ResponseEntity startCalibration(@PathVariable("platform") String platform) throws SeMoDeException {
        this.service.startCalibration(platform);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calibration/deploy/{platform}")
    public ResponseEntity deployCalibration(@PathVariable("platform") String platform) throws SeMoDeException {
        this.service.deployCalibration(platform);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calibration/undeploy/{platform}")
    public ResponseEntity undeployCalibration(@PathVariable("platform") String platform) throws SeMoDeException {
        this.service.undeployCalibration(platform);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/run")
    public ResponseEntity runFunction() throws SeMoDeException {
        this.service.runFunctionLocally();
        return ResponseEntity.ok().build();
    }
}
