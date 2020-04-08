package de.uniba.dsg.serverless.calibration.methods;

import de.uniba.dsg.serverless.util.SeMoDeException;

public interface CalibrationMethods {

    public void stopCalibration();

    public void deployCalibration() throws SeMoDeException;

    public void startCalibration() throws SeMoDeException;
}
