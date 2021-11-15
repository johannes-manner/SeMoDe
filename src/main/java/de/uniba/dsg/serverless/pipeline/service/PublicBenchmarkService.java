package de.uniba.dsg.serverless.pipeline.service;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.BenchmarkConfigRepository;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkDetail;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkVisible;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PublicBenchmarkService {

    private BenchmarkConfigRepository benchmarkConfigRepository;

    @Autowired
    public PublicBenchmarkService(BenchmarkConfigRepository benchmarkConfigRepository) {
        this.benchmarkConfigRepository = benchmarkConfigRepository;
    }


    public Object getVisibleBenchmarks() {
        List<IBenchmarkVisible> benchmarkVisibleList = benchmarkConfigRepository.getBenchmarksPubliclyVisible();
        return benchmarkVisibleList;
    }

    public Optional<BenchmarkConfig> getBenchmarkById(Long benchmarkId) {
        return this.benchmarkConfigRepository.findById(benchmarkId);
    }

    public IPointDto[] getBenchmarkDiagramData(String setupName, Integer version) {
        return this.benchmarkConfigRepository.getBenchmarkExecutionPointsProviderView(setupName, version).toArray(IPointDto[]::new);
    }

    public IBenchmarkDetail[] getBenchmarkExecutionDetailData(String setupName, int versionNumber) {
        return this.benchmarkConfigRepository.getBenchmarkExecutionDetailData(setupName, versionNumber).toArray(IBenchmarkDetail[]::new);
    }
}
