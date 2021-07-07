package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.service.BenchmarkService;
import de.uniba.dsg.serverless.pipeline.service.CalibrationService;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import de.uniba.dsg.serverless.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("setups")
public class SetupController {

    @Autowired
    private SetupService setupService;
    @Autowired
    private BenchmarkService benchmarkService;
    @Autowired
    private CalibrationService calibrationService;

    @GetMapping
    public String getSetups(Model model, @AuthenticationPrincipal User user) {

        log.info("Get all setups...");
        model.addAttribute("setups", this.setupService.getSetups(user));
        model.addAttribute("setupName", new String());

        return "setups";
    }

    @PostMapping
    public String createSetup(String setupName, @AuthenticationPrincipal User user) {
        // create setup
        try {
            this.setupService.createSetup(setupName, user);
        } catch (SeMoDeException e) {
            e.printStackTrace();
        }

        return "redirect:/setups/" + setupName;
    }

    @PostMapping("{name}/delete")
    public String deleteSetup(@PathVariable("name") String setupName, @AuthenticationPrincipal User user) {

        if (this.setupService.checkSetupAccessRights(setupName, user)) {
            // TODO really delete setup
            log.warn("Setup will be deleted. . . " + setupName);

            return "redirect:/setups";
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return "403";
        }
    }

    @GetMapping("{name}")
    public String getSetting(@PathVariable("name") String setupName, Model model, @AuthenticationPrincipal User user) throws SeMoDeException {

        log.info("Setup detail page...");
        if (this.setupService.checkSetupAccessRights(setupName, user)) {
            model.addAttribute("setupConfig", this.setupService.getSetup(setupName));
            model.addAttribute("benchmarkingModes", BenchmarkMode.availableModes);
            model.addAttribute("calibrationPlatforms", CalibrationPlatform.values());
            return "setupDetail";
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return "403";
        }
    }

    @GetMapping("{name}/benchmark")
    public String getBenchmark(@PathVariable("name") String setupName, Model model, @AuthenticationPrincipal User user) throws SeMoDeException {

        log.info("Setup detail benchmark page...");

        if (this.setupService.checkSetupAccessRights(setupName, user)) {
            model.addAttribute("benchmarkConfig", this.benchmarkService.getCurrentBenchmarkForSetup(setupName));
            model.addAttribute("benchmarkingModes", BenchmarkMode.availableModes);
            model.addAttribute("benchmarkingVersions", this.benchmarkService.getBenchmarkVersions(setupName));

            return "benchmarkDetail";
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return "403";
        }
    }

    @PostMapping("{name}/benchmark")
    public String saveBenchmark(BenchmarkConfig config, @PathVariable("name") String setupName, Model model, @AuthenticationPrincipal User user) throws SeMoDeException {

        log.info("Save benchmark config...");
        if (this.setupService.checkSetupAccessRights(setupName, user)) {
            this.benchmarkService.saveBenchmark(config, setupName);

            return "redirect:/setups/" + setupName + "/benchmark";
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return "403";
        }
    }

    @GetMapping("{name}/calibration")
    public String getCalibration(@PathVariable("name") String setupName, Model model, @AuthenticationPrincipal User user) throws SeMoDeException {

        log.info("Setup detail calibration page...");
        if (this.setupService.checkSetupAccessRights(setupName, user)) {
            model.addAttribute("calibrationConfig", this.calibrationService.getCurrentCalibrationConfig(setupName));
            model.addAttribute("benchmarkingModes", BenchmarkMode.availableModes);
            model.addAttribute("localCalibrations", this.calibrationService.getLocalCalibrations(setupName));
            model.addAttribute("providerCalibrations", this.calibrationService.getProviderCalibrations(setupName));

            return "calibrationDetail";
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return "403";
        }
    }

    @PostMapping("{name}/calibration")
    public String saveCalibration(CalibrationConfig config, @PathVariable("name") String setupName, Model model, @AuthenticationPrincipal User user) throws
            SeMoDeException {

        log.info("Save calibration config...");
        if (this.setupService.checkSetupAccessRights(setupName, user)) {
            this.calibrationService.saveCalibration(config, setupName);
            return "redirect:/setups/" + setupName + "/calibration";
        } else {
            log.warn("Access from user '" + user.getUsername() + "' for setup: " + setupName);
            return "403";
        }
    }

    @ExceptionHandler({SeMoDeException.class})
    public void handleCustomException(SeMoDeException e) {
        // TODO think about stack trace here...
        e.printStackTrace();
        log.warn(e.getMessage());
    }
}
