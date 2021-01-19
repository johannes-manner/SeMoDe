package de.uniba.dsg.serverless.spring;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.controller.SetupService;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AsynchronousController {

    @Autowired
    private SetupService service;

    @GetMapping("benchmark/mode/{tag}")
    public BenchmarkMode getMode(@PathVariable(value = "tag") String tag) throws SeMoDeException {
        return BenchmarkMode.fromString(tag);
    }

    @GetMapping("deploy")
    public ResponseEntity deployFunction() throws SeMoDeException {
        log.info("Start deployment...");
        this.service.deployFunctions();
        return ResponseEntity.ok().build();
    }

    @GetMapping("undeploy")
    public ResponseEntity undeployFunction() throws SeMoDeException {
        this.service.undeployFunctions();
        return ResponseEntity.ok().build();
    }
}
