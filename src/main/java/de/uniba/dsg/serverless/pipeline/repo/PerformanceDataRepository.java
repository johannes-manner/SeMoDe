package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerformanceDataRepository extends JpaRepository<PerformanceData, Long> {

    @Query(value = "SELECT memory_size as x, precise_duration as y\n" +
            "FROM performance_data " +
            "WHERE benchmark_config_id = ?1", nativeQuery = true)
    List<IPointDto> getDataPointsByBenchmarkId(Long id);
}
