package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalibrationEventRepository extends JpaRepository<CalibrationEvent, Long> {
}
