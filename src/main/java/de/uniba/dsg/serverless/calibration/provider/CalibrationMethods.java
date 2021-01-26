package de.uniba.dsg.serverless.calibration.provider;

import de.uniba.dsg.serverless.util.SeMoDeException;

public interface CalibrationMethods {

    public void undeployCalibration();

    public void deployCalibration() throws SeMoDeException;

    public void startCalibration() throws SeMoDeException;
}
