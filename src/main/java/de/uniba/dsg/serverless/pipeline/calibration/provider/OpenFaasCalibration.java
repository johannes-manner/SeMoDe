package de.uniba.dsg.serverless.pipeline.calibration.provider;

import de.uniba.dsg.serverless.pipeline.calibration.Calibration;
import de.uniba.dsg.serverless.pipeline.calibration.LinpackParser;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.calibration.model.LinpackResult;
import de.uniba.dsg.serverless.pipeline.calibration.util.QuotaCalculator;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.openfaas.OpenFaasConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OpenFaasCalibration implements CalibrationMethods {

    private final OpenFaasConfig openFaasConfiguration;
    private final Calibration calibration;

    public OpenFaasCalibration(OpenFaasConfig openFaasConfiguration) throws SeMoDeException {
        this.openFaasConfiguration = openFaasConfiguration;
        this.calibration = new Calibration(openFaasConfiguration.getDockerUsername(), CalibrationPlatform.OPEN_FAAS);
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

        List<CalibrationEvent> events = new ArrayList<>();

        for (int i = 0; i < this.openFaasConfiguration.getNumberOfCalibrations(); i++) {
            for (Double quota : QuotaCalculator.calculateQuotas(this.openFaasConfiguration.getIncrements())) {
                log.info("Calibration run: " + i + " - quota: " + quota);
                String functionName = generateFunctionName(quota);
                WebTarget openFaasRequest = ClientBuilder.newClient().target(openFaasConfiguration.getFileTransferURL() + functionName + "-" + i + ".log");
                Response r = openFaasRequest.request()
                        .get();

                List<String> responseEntity = r.readEntity(List.class);
                String linpackResult = responseEntity.stream().collect(Collectors.joining("\n"));
                Path logFile = Paths.get(calibration.calibrationLogs.toString(), functionName + "-" + i + ".log");
                try {
                    Files.write(logFile, linpackResult.getBytes(StandardCharsets.UTF_8));
                } catch (FileAlreadyExistsException e) {
                    throw new SeMoDeException("Calibration was already executed", e);
                } catch (IOException e) {
                    throw new SeMoDeException("Cannot write files to " + calibration.calibrationLogs.toString() + " directory", e);
                }
                LinpackParser parser = new LinpackParser(logFile);
                LinpackResult result = parser.parseLinpack();
                events.add(new CalibrationEvent(i, quota, result, CalibrationPlatform.OPEN_FAAS));
            }
        }
        return events;
    }

    private String generateFunctionName(Double quota) {
        return openFaasConfiguration.getFunctionName() + "-" + (int) (quota * 1000);
    }
}
