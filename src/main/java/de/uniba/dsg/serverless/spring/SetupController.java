package de.uniba.dsg.serverless.spring;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("setups")
public class SetupController {

    @Value("${semode.setups.path}")
    private String setups;

    @GetMapping
    public String getSetups(Model model) {

        List<String> setupList = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(this.setups))) {
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    setupList.add(p.getFileName().toString());
                }
            });
        } catch (IOException e) {
            // TODO error handling and show an error with a pop up window
        }

        model.addAttribute("setups", setupList);
        return "setups";
    }

    @PostMapping("{name}/delete")
    public String deleteSetup(@PathVariable("name") String setupName) {

        // TODO really delete setup
        log.warn("Setup will be deleted. . . " + setupName);

        return "redirect:/setups";
    }

    @GetMapping("{name}")
    public String getSetting(@PathVariable("name") String setupName, Model model) {

        return "setupDetail";
    }
}
