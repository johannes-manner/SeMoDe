package de.uniba.dsg.serverless.pipeline.calibration.provider;

import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

import java.util.List;

public interface CalibrationMethods {

    public void undeployCalibration();

    public void deployCalibration() throws SeMoDeException;

    public List<CalibrationEvent> startCalibration() throws SeMoDeException;
}
