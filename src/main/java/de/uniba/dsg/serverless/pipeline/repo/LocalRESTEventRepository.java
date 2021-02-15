package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalRESTEventRepository extends JpaRepository<LocalRESTEvent, Long> {
}
