package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CalibrationEventRepository extends JpaRepository<CalibrationEvent, Long> {

    public List<CalibrationEvent> findByConfigId(Long configId);

    @Query(value = "SELECT ce.cpu_or_memory_quota as x, ce.gflops as y\n" +
            "FROM calibration_config cc LEFT OUTER JOIN calibration_event ce ON cc.id = ce.config_id\n" +
            "WHERE cc.id = ?1 AND cc.setup_name = ?2", nativeQuery = true)
    public List<IPointDto> getCalibrationPoints(Integer calibrationConfigId, String setupName);
}
