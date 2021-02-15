package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalibrationConfigRepository extends JpaRepository<CalibrationConfig, Long> {

    @EntityGraph(value = "CalibrationConfig.calibrationEvents")
    public List<CalibrationConfig> findDistinctCalibrationConfigBySetupName(String setupName);
}
