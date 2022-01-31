package de.uniba.dsg.serverless.pipeline.calibration.provider;

import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.model.config.openfaas.OpenFaasConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OpenFaasCalibration implements CalibrationMethods {

    private final OpenFaasConfig openFaasConfiguration;

    public OpenFaasCalibration(OpenFaasConfig openFaasConfiguration) {
        this.openFaasConfiguration = openFaasConfiguration;
    }

    @Override

    public void undeployCalibration() {
        log.info("An undeployment for OpenFaaS calibration is not supported yet. "
                + "Find the manual config description on the github page.");
    }

    @Override
    public void deployCalibration() throws SeMoDeException {
        log.info("A deployment for OpenFaaS calibration is not supported yet. "
                + "Find the manual config description o n the github page.");
    }

    @Override
    public List<CalibrationEvent> startCalibration() throws SeMoDeException {
        return null;
    }
}
