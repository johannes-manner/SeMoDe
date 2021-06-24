package de.uniba.dsg.serverless.pipeline.rest.controller.semode;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AsynchronousBenchmarkController {

    @Autowired
    private SetupService service;

    @GetMapping("semode/v1/{setup}/benchmark/deploy")
    public ResponseEntity deployFunction(@PathVariable("setup") String setupName,
                                         @AuthenticationPrincipal User user) throws SeMoDeException {
        log.info("Start deployment...");
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.service.deployFunctions(setupName);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/benchmark/undeploy")
    public ResponseEntity undeployFunction(@PathVariable("setup") String setupName,
                                           @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.service.undeployFunctions(setupName);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }

    @GetMapping("semode/v1/{setup}/benchmark/execute")
    public ResponseEntity benchmark(@PathVariable("setup") String setupName,
                                    @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.service.executeBenchmark(setupName);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/benchmark/fetch")
    public ResponseEntity fetch(@PathVariable("setup") String setupName,
                                @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.service.fetchPerformanceData(setupName);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/benchmark/mode/{tag}")
    public BenchmarkMode getMode(@PathVariable(value = "tag") String tag) throws SeMoDeException {
        return BenchmarkMode.fromString(tag);
    }

    @GetMapping("semode/v1/{setup}/benchmark/version/{version}")
    public ResponseEntity<BenchmarkConfig> getBenchmarkConfigByVersion(@PathVariable("setup") String setupName,
                                                                       @PathVariable(value = "version") Integer version,
                                                                       @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.service.getBenchmarkConfigBySetupAndVersion(setupName, version));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }

    @GetMapping("semode/v1/{setup}/benchmark/version/{version}/data")
    public ResponseEntity<IPointDto[]> getBenchmarkData(@PathVariable(value = "setup") String setupName,
                                                        @PathVariable(value = "version") Integer version,
                                                        @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.service.getBenchmarkDataByVersion(setupName, version));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("semode/v1/{setup}/benchmark/visible/{version}")
    public ResponseEntity changePublicVisibility(@PathVariable(value = "setup") String setupName,
                                                 @PathVariable(value = "version") int version,
                                                 @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.service.changePublicVisiblityPropertyForBenchmarkVersion(setupName, version);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("semode/v1/{setup}/benchmark/description/{version}")
    public ResponseEntity changeBenchmarkDescription(@PathVariable(value = "setup") String setupName,
                                                     @PathVariable(value = "version") int version,
                                                     String newDescription,
                                                     @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            this.service.changeDescriptionForBenchmarkVersion(setupName, version, newDescription);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
