package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetupConfigRepository extends JpaRepository<SetupConfig, String> {

    List<SetupConfig> findByOwner(User owner);

    SetupConfig findBySetupName(String setupName);
}
