package de.uniba.dsg.serverless.spring.repo;

import java.util.Optional;

import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderEventRepository extends JpaRepository<ProviderEvent, Long> {

    public Optional<ProviderEvent> findByPlatformId(String platformId);
}
