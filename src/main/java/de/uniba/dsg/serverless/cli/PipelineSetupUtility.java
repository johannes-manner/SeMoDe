package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.pipeline.controller.SetupService;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

@Deprecated
// TODO check if we can generate configuration and beans out of the utilities...
public class PipelineSetupUtility extends CustomUtility {

    // TODO check if we can use service here
    private SetupService controller;

    public PipelineSetupUtility(final String name) {
        super(name);
    }

    @Override
    public void start(List<String> args) {
        // TODO implementation missing - invoke executeRunCommand
    }


    private void executeRunCommand(final String command) throws SeMoDeException {
        switch (command) {
            case "deployBenchmark":
                this.controller.deployFunctions();
                break;
            case "executeBenchmark":
                this.controller.executeBenchmark();
                break;
            case "fetchBenchmark":
                this.controller.fetchPerformanceData();
                break;
            case "undeployFunctions":
                this.controller.undeployFunctions();
                break;
            // calibration options
            case "deployCalibration":
                this.controller.deployCalibration(""); //TODO
                break;
            case "startCalibration":
                this.controller.startCalibration(""); // TODO
                break;
            case "undeployCalibration":
//                this.controller.undeployCalibration();  // TODO not implemented yet...
                break;
            case "mapping":
                this.controller.computeMapping();
                break;
            case "run":
                this.controller.runFunctionLocally();
                break;
            default:
                throw new SeMoDeException(
                        "The command " + command + " is not available. Check your spelling or open an Issue on github.",
                        new NotImplementedException(""));
        }
    }
}
