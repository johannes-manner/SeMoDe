package de.uniba.dsg.serverless.pipeline.rest.service;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.BenchmarkConfigRepository;
import de.uniba.dsg.serverless.pipeline.repo.PerformanceDataRepository;
import de.uniba.dsg.serverless.pipeline.rest.dto.BenchmarkConfigDTO;
import de.uniba.dsg.serverless.pipeline.rest.dto.BenchmarkDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.ForbiddenException;
import java.util.Optional;

@Slf4j
@Service
public class RestBenchmarkService {

    private BenchmarkConfigRepository benchmarkConfigRepository;
    private PerformanceDataRepository performanceDataRepository;

    @Autowired
    public RestBenchmarkService(BenchmarkConfigRepository benchmarkConfigRepository,
                                PerformanceDataRepository performanceDataRepository) {
        this.benchmarkConfigRepository = benchmarkConfigRepository;
        this.performanceDataRepository = performanceDataRepository;
    }

    public BenchmarkConfigDTO getBenchmarkConfig(Long id) {
        Optional<BenchmarkConfig> config = benchmarkConfigRepository.findById(id);

        if (config.isEmpty()) {
            throw new EntityNotFoundException();
        } else if (config.get().isVersionVisible() == false) {
            throw new ForbiddenException();
        } else {
            return new BenchmarkConfigDTO(config.get());
        }
    }

    public BenchmarkDataDTO getBenchmarkData(Long id) {
        Optional<BenchmarkConfig> config = benchmarkConfigRepository.findById(id);

        if (config.isEmpty()) {
            throw new EntityNotFoundException();
        } else if (config.get().isVersionVisible() == false) {
            throw new ForbiddenException();
        } else {
            return new BenchmarkDataDTO(id, performanceDataRepository.getDataPointsByBenchmarkId(id));
        }
    }
}
