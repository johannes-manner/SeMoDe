package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalibrationEventRepository extends JpaRepository<CalibrationEvent, Long> {

    public List<CalibrationEvent> findByConfigId(Long configId);
}
