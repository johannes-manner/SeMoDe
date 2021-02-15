package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.calibration.profiling.ProfileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRecordRepository extends JpaRepository<ProfileRecord, Long> {
}
