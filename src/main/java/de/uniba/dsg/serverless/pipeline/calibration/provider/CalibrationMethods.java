package de.uniba.dsg.serverless.pipeline.calibration.provider;

import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

public interface CalibrationMethods {

    public void undeployCalibration();

    public void deployCalibration() throws SeMoDeException;

    public void startCalibration() throws SeMoDeException;
}
