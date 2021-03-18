package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceDataRepository extends JpaRepository<PerformanceData, Long> {
}
