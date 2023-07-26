package de.uniba.dsg.serverless.pipeline.controller.async;

import de.uniba.dsg.serverless.pipeline.controller.async.dto.IPointDtoClass;
import de.uniba.dsg.serverless.pipeline.controller.async.dto.ProfileDataDto;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigId;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.service.CalibrationService;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import de.uniba.dsg.serverless.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class AsynchronousCalibrationController {

    @Autowired
    private SetupService service;
    @Autowired
    private CalibrationService calibrationService;

    @Value("${aws.lambda.price}")
    private double awsLambdaPricePerMBAndMS;
    @Value("${aws.cpu.equivalents}")
    private String awsCPUEquivalents;

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

    @GetMapping("semode/v1/{setup}/profiles/{calibrationConfigId}/names")
    public ResponseEntity<List<String>> getExecutedFunctionNamesByCalibration(@PathVariable(value = "setup") String setupName,
                                                                              @PathVariable("calibrationConfigId") Integer id,
                                                                              @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(this.calibrationService.getFunctionNamesForProfile(id));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("semode/v1/{setup}/profiles/{calibrationConfigId}")
    public ResponseEntity<ProfileDataDto> getProfilePointsForSetupAndCalibration(@PathVariable(value = "setup") String setupName,
                                                                                 @PathVariable("calibrationConfigId") Integer id,
                                                                                 @RequestParam Double avg,
                                                                                 @RequestParam String function,
                                                                                 @AuthenticationPrincipal User user) {
        if (this.service.checkSetupAccessRights(setupName, user)) {

            // choose for a specific function
            // if function is blank use the first entry as default
            if (function.isBlank()) {
                function = this.calibrationService.getFunctionNamesForProfile(id).stream().findFirst().orElse("");
            }
            List<IPointDto> profilingData = this.calibrationService.getProfilePointsForSetupAndCalibration(setupName, id, function);
            Map<Double, List<Double>> profilingDataPerMB = new HashMap<>();

            for (IPointDto p : profilingData) {
                profilingDataPerMB.putIfAbsent(p.getX(), new ArrayList<>());
                profilingDataPerMB.get(p.getX()).add(p.getY());
            }

            double avgValue = -1.0;
            double minMBValue = profilingDataPerMB.keySet().stream().min(Double::compareTo).orElse(-1.0);
            double maxMBValue = profilingDataPerMB.keySet().stream().max(Double::compareTo).orElse(-1.0);

            // compute the average execution time for the simulation data
            Map<Double, Double> averageExecutionTimePerMB = new HashMap<>();
            for (double memoryConfig : profilingDataPerMB.keySet()) {
                averageExecutionTimePerMB.put(memoryConfig,
                        profilingDataPerMB.get(memoryConfig).stream().reduce(-1.0, (a1, a2) -> a1 + a2) /
                                profilingDataPerMB.get(memoryConfig).stream().count());
            }

            // check if the avg value is in this new list, otherwise use the minimum
            if (averageExecutionTimePerMB.containsKey(avg)) {
                avgValue = averageExecutionTimePerMB.get(avg);
            } else {
                avgValue = averageExecutionTimePerMB.get(minMBValue);
                avg = minMBValue;
            }

            // compute optimal price when assuming doubling resources results in halve the price
            List<IPointDto> avgCurve = new ArrayList<>();
            for (double i = minMBValue; i <= maxMBValue; i++) {
                avgCurve.add(new IPointDtoClass(i, avgValue * avg / i));
            }

            // compute price based on the simulation data
            List<IPointDto> simulatedPrice = new ArrayList<>();
            for (Double memorySetting : averageExecutionTimePerMB.keySet()) {
                simulatedPrice.add(new IPointDtoClass(memorySetting, awsLambdaPricePerMBAndMS * memorySetting * averageExecutionTimePerMB.get(memorySetting)));
            }
            simulatedPrice.sort(new Comparator<IPointDto>() {
                @Override
                public int compare(IPointDto o1, IPointDto o2) {
                    return (int) (o1.getX() - o2.getX());
                }
            });

            // getCPUMemoryEquivalents for drawing vertical lines
            Map<Integer, Integer> localCpuMemoryEquivalents = this.calibrationService.computeCPUMemoryEquivalents(id.longValue(), maxMBValue);

            return ResponseEntity.ok(new ProfileDataDto(
                    profilingData.toArray(IPointDto[]::new),
                    avgCurve.toArray(IPointDto[]::new),
                    profilingDataPerMB.keySet().stream().mapToDouble(d -> d).sorted().toArray(),
                    avg,
                    localCpuMemoryEquivalents,
                    this.filterProviderCPUMemoryEquivalents(maxMBValue),
                    simulatedPrice.toArray(IPointDto[]::new)));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private Map<Integer, Integer> filterProviderCPUMemoryEquivalents(double maxMBValue) {
        Map<Integer, Integer> providerCPUMemoryEquivalents = new HashMap<>();
        int cpu = 1;
        for (String memoryEquivalentString : awsCPUEquivalents.split(",")) {
            int memoryEquivalent = Integer.valueOf(memoryEquivalentString);
            if (memoryEquivalent < maxMBValue) {
                providerCPUMemoryEquivalents.put(cpu, memoryEquivalent);
            }
            cpu++;
        }
        return providerCPUMemoryEquivalents;
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

    @GetMapping("semode/v1/{setup}/mapping/gflops")
    public ResponseEntity<String> computeGflopsBasedOnProvider(@PathVariable(value = "setup") String setupName,
                                                               @RequestParam(name = "gflops") String gflops,
                                                               @AuthenticationPrincipal User user) throws SeMoDeException {
        if (this.service.checkSetupAccessRights(setupName, user)) {
            return ResponseEntity.ok(
                    this.calibrationService.computeGflopsMapping(setupName, gflops).stream()
                            .map(d -> "" + d)
                            .collect(Collectors.joining(",")));
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


}
