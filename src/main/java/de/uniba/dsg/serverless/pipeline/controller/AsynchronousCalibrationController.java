package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}
