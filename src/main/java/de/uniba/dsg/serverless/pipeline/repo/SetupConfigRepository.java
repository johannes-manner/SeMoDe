package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetupConfigRepository extends JpaRepository<SetupConfig, String> {
}
