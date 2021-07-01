package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigEventAggregate;
import de.uniba.dsg.serverless.pipeline.repo.projection.ICalibrationConfigId;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CalibrationConfigRepository extends JpaRepository<CalibrationConfig, Long> {

    @Query(value = "SELECT DISTINCT cc.id, cc.version_number as versionNumber, ce.platform\n" +
            "FROM calibration_config cc LEFT OUTER JOIN calibration_event ce ON cc.id = ce.config_id\n" +
            "WHERE cc.setup_name = ?1 and ce.id IS NOT NULL\n" +
            "ORDER BY version_number", nativeQuery = true)
    public List<ICalibrationConfigEventAggregate> findCalibrationEventsBySetupName(String setupName);

    CalibrationConfig findCalibrationConfigBySetupNameAndVersionNumber(String setup, Integer version);

    @Query(value = "select distinct cc.id as calibrationId, cc.version_number as version \n" +
            "from calibration_config cc INNER JOIN profile_record pr on cc.id = pr.calibration_config_id\n" +
            "where cc.setup_name = ?1\n" +
            "order by cc.id", nativeQuery = true)
    List<ICalibrationConfigId> getCalibrationConfigIdsWithAssociatedProfiles(String setupName);

    @Query(value = "select pr.memory_size as x, pr.duration_inms as y\n" +
            "from calibration_config cc INNER JOIN profile_record pr on cc.id = pr.calibration_config_id\n" +
            "where cc.setup_name = ?1 and cc.id = ?2\n" +
            "order by cc.id", nativeQuery = true)
    List<IPointDto> getProfilePointsBySetupAndCalibrationId(String setup, Integer id);
}
