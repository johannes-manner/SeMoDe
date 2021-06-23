package de.uniba.dsg.serverless.pipeline.rest.controller.semode;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AsynchronousBenchmarkController {

    @Autowired
    private SetupService service;

    @GetMapping("semode/v1/{setup}/benchmark/deploy")
    public ResponseEntity deployFunction(@PathVariable("setup") String setup) throws SeMoDeException {
        log.info("Start deployment...");
        this.service.deployFunctions(setup);
        return ResponseEntity.ok().build();
    }

    @GetMapping("semode/v1/{setup}/benchmark/undeploy")
    public ResponseEntity undeployFunction(@PathVariable("setup") String setup) throws SeMoDeException {
        this.service.undeployFunctions(setup);
        return ResponseEntity.ok().build();
    }

    @GetMapping("semode/v1/{setup}/benchmark/execute")
    public ResponseEntity benchmark(@PathVariable("setup") String setup) throws SeMoDeException {
        this.service.executeBenchmark(setup);
        return ResponseEntity.ok().build();
    }

    @GetMapping("semode/v1/{setup}/benchmark/fetch")
    public ResponseEntity fetch(@PathVariable("setup") String setup) throws SeMoDeException {
        this.service.fetchPerformanceData(setup);
        return ResponseEntity.ok().build();
    }

    @GetMapping("semode/v1/benchmark/mode/{tag}")
    public BenchmarkMode getMode(@PathVariable(value = "tag") String tag) throws SeMoDeException {
        return BenchmarkMode.fromString(tag);
    }

    @GetMapping("semode/v1/{setup}/benchmark/version/{version}")
    public BenchmarkConfig getBenchmarkConfigByVersion(@PathVariable("setup") String setup, @PathVariable(value = "version") Integer version) {
        return this.service.getBenchmarkConfigBySetupAndVersion(setup, version);
    }

    @GetMapping("semode/v1/{setup}/benchmark/version/{version}/data")
    public IPointDto[] getBenchmarkData(@PathVariable(value = "setup") String setupName, @PathVariable(value = "version") Integer version) {
        return this.service.getBenchmarkDataByVersion(setupName, version);
    }

    @PostMapping("semode/v1/{setup}/benchmark/visible/{version}")
    public ResponseEntity changePublicVisibility(@PathVariable(value = "setup") String setupName, @PathVariable(value = "version") int version) {
        this.service.changePublicVisiblityPropertyForBenchmarkVersion(setupName, version);
        return ResponseEntity.ok().build();
    }

    @PostMapping("semode/v1/{setup}/benchmark/description/{version}")
    public ResponseEntity changeBenchmarkDescription(@PathVariable(value = "setup") String setupName, @PathVariable(value = "version") int version, String newDescription) {
        this.service.changeDescriptionForBenchmarkVersion(setupName, version, newDescription);
        return ResponseEntity.ok().build();
    }
}
