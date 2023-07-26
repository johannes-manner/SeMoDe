package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.calibration.profiling.ProfileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProfileRecordRepository extends JpaRepository<ProfileRecord, Long> {

    @Query(value = "select distinct function_name\n" +
            "from profile_record\n" +
            "where calibration_config_id =?1", nativeQuery = true)
    List<String> findDistinctFunctionNameByCalibrationConfigId(Long calibrationConfigId);
}
