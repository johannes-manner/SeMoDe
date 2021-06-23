package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.service.SetupService;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CliSetupService implements CustomUtility {

    private SetupService setupService;

    @Autowired
    public CliSetupService(SetupService setupService) {
        this.setupService = setupService;
    }

    @Override
    public void start(List<String> args) {

        if (args.size() < 2) {
            log.warn("Parameter error: (1) Setup name (2) Command");
            log.info("Available commands" + "\n" +
                    "executeBenchmark" + "\n" +
                    "fetchBenchmark" + "\n" +
                    "startAwsCalibration" + "\n" +
                    "startLocalCalibration" + "\n" +
                    "printMappingInfo" + "\n" +
                    "runFunctionLocally" + "\n"
            );
            return;
        }
        try {
            log.info("Load setup '" + args.get(0) + "' and execute command '" + args.get(1) + "'...");
            this.executeRunCommand(args.get(0), args.get(1));
        } catch (SeMoDeException e) {
            log.warn(e.getMessage(), e.getCause() != null ? e.getCause().toString() : "");
        }
    }

    @Override
    public String getName() {
        return "setup";
    }


    private void executeRunCommand(final String setupName, final String command) throws SeMoDeException {
        switch (command) {
            case "executeBenchmark":
                this.setupService.executeBenchmark(setupName);
                break;
            case "fetchBenchmark":
                this.setupService.fetchPerformanceData(setupName);
                break;
            // calibration options
            case "startAwsCalibration":
                this.setupService.startCalibration(setupName, CalibrationPlatform.AWS.getText());
                break;
            case "startLocalCalibration":
                this.setupService.startCalibration(setupName, CalibrationPlatform.LOCAL.getText());
                break;
            // mapping and run function locally
            case "printMappingInfo":
                this.setupService.computeMapping(setupName);
                break;
            case "runFunctionLocally":
                this.setupService.runFunctionLocally(setupName);
                break;
            default:
                throw new SeMoDeException(
                        "The command " + command + " is not available. Check your spelling or open an Issue on github.",
                        new NotImplementedException(""));
        }
    }
}
