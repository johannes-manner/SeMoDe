package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("setups")
public class SetupController {

    @Autowired
    private SetupService setupService;

    @GetMapping
    public String getSetups(Model model) {

        log.info("Get all setups...");
        model.addAttribute("setups", this.setupService.getSetupNames());
        model.addAttribute("setupName", new String());

        return "setups";
    }

    @PostMapping
    public String createSetup(String setupName) {
        // create setup
        try {
            this.setupService.createSetup(setupName);
        } catch (SeMoDeException e) {
            e.printStackTrace();
        }

        return "redirect:/setups/" + setupName;
    }

    @PostMapping("{name}/update")
    // TODO valid
    public String updateSetup(/*@Valid*/ SetupConfig setupConfig, @PathVariable("name") String setupName, Errors errors) throws SeMoDeException {
        log.info("Setup update...");

        log.info(setupConfig.toString());
        this.setupService.updateSetup(setupConfig);

        return "redirect:/setups/" + setupName;
    }

    @PostMapping("{name}/delete")
    public String deleteSetup(@PathVariable("name") String setupName) {

        // TODO really delete setup
        log.warn("Setup will be deleted. . . " + setupName);

        return "redirect:/setups";
    }

    @GetMapping("{name}")
    public String getSetting(@PathVariable("name") String setupName, Model model) throws SeMoDeException {

        log.info("Setup detail page...");

        model.addAttribute("setupConfig", this.setupService.getSetup(setupName));
        model.addAttribute("benchmarkingModes", BenchmarkMode.availableModes);
        model.addAttribute("calibrationPlatforms", CalibrationPlatform.values());

        return "setupDetail";
    }

    @GetMapping("{name}/benchmark")
    public String getBenchmark(@PathVariable("name") String setupName, Model model) throws SeMoDeException {

        log.info("Setup detail benchmark page...");

        model.addAttribute("benchmarkConfig", this.setupService.getCurrentBenchmark(setupName));
        model.addAttribute("benchmarkingModes", BenchmarkMode.availableModes);
        model.addAttribute("benchmarkingVersions", this.setupService.getBenchmarkVersions(setupName));

        return "benchmarkDetail";
    }

    @PostMapping("{name}/benchmark")
    public String saveBenchmark(BenchmarkConfig config, @PathVariable("name") String setupName, Model model) throws SeMoDeException {

        log.info("Save benchmark config...");

        this.setupService.saveBenchmark(config, setupName);

        return "redirect:/setups/" + setupName + "/benchmark";
    }

    @GetMapping("{name}/calibration")
    public String getCalibration(@PathVariable("name") String setupName, Model model) throws SeMoDeException {

        log.info("Setup detail calibration page...");

        model.addAttribute("calibrationConfig", this.setupService.getCurrentCalibrationConfig(setupName));
        model.addAttribute("benchmarkingModes", BenchmarkMode.availableModes);
        model.addAttribute("localCalibrations", this.setupService.getLocalCalibrations(setupName));
        model.addAttribute("providerCalibrations", this.setupService.getProviderCalibrations(setupName));

        return "calibrationDetail";
    }

    @PostMapping("{name}/calibration")
    public String saveCalibration(CalibrationConfig config, @PathVariable("name") String setupName, Model model) throws SeMoDeException {

        log.info("Save calibration config...");

        this.setupService.saveCalibration(config, setupName);

        return "redirect:/setups/" + setupName + "/calibration";
    }

    @ExceptionHandler({SeMoDeException.class})
    public void handleCustomException(SeMoDeException e) {
        // TODO show all exception messages from all stacks
        e.printStackTrace();
        log.warn(e.getMessage());
    }
}
