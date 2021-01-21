package de.uniba.dsg.serverless.spring.repo;

import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderEventRepository extends JpaRepository<ProviderEvent, Long> {

}
