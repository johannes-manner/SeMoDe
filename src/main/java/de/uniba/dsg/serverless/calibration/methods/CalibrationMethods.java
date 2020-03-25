package de.uniba.dsg.serverless.calibration.methods;

import de.uniba.dsg.serverless.util.SeMoDeException;

public interface CalibrationMethods {

    public void performCalibration() throws SeMoDeException;

    public void stopCalibration();
}
