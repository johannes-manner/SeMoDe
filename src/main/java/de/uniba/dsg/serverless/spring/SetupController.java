package de.uniba.dsg.serverless.spring;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.pipeline.controller.SetupService;
import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String createSetup(String setupName, Model model) {
        // TODO add validation
//        if (errors.hasErrors()) {
//            log.info("Setup " + setupName + " already present.");
//            model.addAttribute("setups", this.setupService.getSetupNames());
//            return "setups";
//        }

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

        return "setupDetail";
    }

    @ExceptionHandler({SeMoDeException.class})
    public void handleCustomException(SeMoDeException e) {
        // TODO show all exception messages from all stacks
        e.printStackTrace();
        log.warn(e.getMessage());
    }
}
