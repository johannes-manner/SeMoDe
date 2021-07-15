package de.uniba.dsg.serverless.pipeline.controller;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import de.uniba.dsg.serverless.pipeline.service.PublicBenchmarkService;
import de.uniba.dsg.serverless.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
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

    @GetMapping("{id}/version/{version}/data.csv")
    public void getBenchmarkDataCsv(HttpServletResponse response,
                                    @PathVariable(value = "id") Long benchmarkId,
                                    @PathVariable(value = "version") Integer version,
                                    @AuthenticationPrincipal User user) {

        Optional<BenchmarkConfig> benchmarkConfigOptional = this.benchmarkService.getBenchmarkById(benchmarkId);
        if (benchmarkConfigOptional.isPresent()) {
            BenchmarkConfig benchmarkConfig = benchmarkConfigOptional.get();
            if (benchmarkConfig.isVersionVisible()) {
                try {
                    log.info("Download data for benchmark config: " + benchmarkConfig.getId() + " and version: " + benchmarkConfig.getVersionNumber());
                    writeCsvToHttpResponse(response, benchmarkConfig);
                } catch (IOException e) {
                    log.warn("Writer of the response not accessible");
                    throw new BadRequestException();
                }
            } else {
                throw new ForbiddenException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    private void writeCsvToHttpResponse(HttpServletResponse response, BenchmarkConfig benchmarkConfig) throws IOException {
        // metadata of the response
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=data.csv");

        // header of the csv file
        response.getWriter().write("Memory Setting,Execution Time\n");

        // data entries of the csv
        for (IPointDto dataPoint : benchmarkService.getBenchmarkDiagramData(
                benchmarkConfig.getSetupName(),
                benchmarkConfig.getVersionNumber())) {
            response.getWriter().write(dataPoint.getX() + "," + dataPoint.getY() + "\n");
        }
    }
}
