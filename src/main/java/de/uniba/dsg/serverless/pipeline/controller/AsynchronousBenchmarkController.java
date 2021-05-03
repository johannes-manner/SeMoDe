package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// TODO get him standalone!!
@Slf4j
@RestController
public class AsynchronousBenchmarkController {

    @Autowired
    private SetupService service;

    @GetMapping("benchmark/deploy")
    public ResponseEntity deployFunction() throws SeMoDeException {
        log.info("Start deployment...");
        this.service.deployFunctions();
        return ResponseEntity.ok().build();
    }

    @GetMapping("benchmark/undeploy")
    public ResponseEntity undeployFunction() throws SeMoDeException {
        this.service.undeployFunctions();
        return ResponseEntity.ok().build();
    }

    @GetMapping("benchmark/execute")
    public ResponseEntity benchmark() throws SeMoDeException {
        this.service.executeBenchmark();
        return ResponseEntity.ok().build();
    }

    @GetMapping("benchmark/fetch")
    public ResponseEntity fetch() throws SeMoDeException {
        this.service.fetchPerformanceData();
        return ResponseEntity.ok().build();
    }

    @GetMapping("benchmark/mode/{tag}")
    public BenchmarkMode getMode(@PathVariable(value = "tag") String tag) throws SeMoDeException {
        return BenchmarkMode.fromString(tag);
    }

    @GetMapping("benchmark/version/{version}")
    public BenchmarkConfig getBenchmarkConfigByVersion(@PathVariable(value = "version") Integer version) {
        return this.service.getBenchmarkConfigByVersion(version);
    }

    @GetMapping("{setup}/benchmark/version/{version}/data")
    public IPointDto[] getBenchmarkData(@PathVariable(value = "setup") String setupName, @PathVariable(value = "version") Integer version) {
        return this.service.getBenchmarkDataByVersion(setupName, version);
    }
}
