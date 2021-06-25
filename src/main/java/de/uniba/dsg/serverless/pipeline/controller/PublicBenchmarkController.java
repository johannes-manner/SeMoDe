package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.service.PublicBenchmarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/benchmarks")
public class PublicBenchmarkController {

    @Autowired
    private PublicBenchmarkService benchmarkService;

    @GetMapping
    public String getPubliclyAvailableBenchmarks(Model model) {
        model.addAttribute("benchmarks", benchmarkService.getVisibleBenchmarks());
        return "benchmarksVisible";
    }

    @GetMapping("{id}")
    public String getPubliclyAvailabeBenchmark(Model model, @PathVariable("id") Long benchmarkId) {
        Optional<BenchmarkConfig> benchmarkConfigOptional = this.benchmarkService.getBenchmarkById(benchmarkId);
        if (benchmarkConfigOptional.isPresent()) {
            BenchmarkConfig benchmarkConfig = benchmarkConfigOptional.get();
            if (benchmarkConfig.isVersionVisible()) {
                model.addAttribute("benchmarkConfig", benchmarkConfig);
                model.addAttribute("diagramData",
                        benchmarkService.getBenchmarkDiagramData(
                                benchmarkConfig.getSetupName(),
                                benchmarkConfig.getVersionNumber()));
                return "benchmarkDetailVisible";
            } else {
                log.warn("Tried to access benchmark id : " + benchmarkId);
                return "403";
            }
        } else {
            return "404";
        }
    }
}
