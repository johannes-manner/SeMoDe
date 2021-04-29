package de.uniba.dsg.serverless.pipeline.repo.projection;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;

public interface ICalibrationConfigEventAggregate {
    public Long getId();

    public Integer getVersionNumber();

    public CalibrationPlatform getPlatform();
}
