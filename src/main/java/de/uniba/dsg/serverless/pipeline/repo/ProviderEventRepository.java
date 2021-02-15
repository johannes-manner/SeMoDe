package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.benchmark.model.ProviderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderEventRepository extends JpaRepository<ProviderEvent, Long> {

    public Optional<ProviderEvent> findByPlatformId(String platformId);
}
