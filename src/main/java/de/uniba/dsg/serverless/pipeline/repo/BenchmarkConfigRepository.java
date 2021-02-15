package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenchmarkConfigRepository extends JpaRepository<BenchmarkConfig, Long> {

    @EntityGraph(value = "BenchmarkConfig.localExecutionEvents")
    public List<BenchmarkConfig> findBySetupNameOrderByVersionNumberDesc(String setupName);

}
