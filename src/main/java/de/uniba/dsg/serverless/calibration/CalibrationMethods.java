package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;

public interface CalibrationMethods {

    public void performCalibration() throws SeMoDeException;

    public void stopCalibration();
}
