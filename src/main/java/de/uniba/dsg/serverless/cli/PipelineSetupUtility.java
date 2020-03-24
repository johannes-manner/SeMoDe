package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.controller.PipelineSetupController;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Scanner;

public class PipelineSetupUtility extends CustomUtility {

    public static final Scanner scanner = new Scanner(System.in);

    private PipelineSetupController controller;

    public PipelineSetupUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {
        if (args.size() < 1) {
            System.err.println("Wrong parameter size: " + "\nUSAGE: SETUP_NAME");
            return;
        }
        try {
            this.loadOrInitSetup(args.get(0));
        } catch (final SeMoDeException e) {
            System.err.println(e);
            return;
        }
        this.printRunCommandUsage();
        String command = scanner.nextLine();
        while (!"exit".equals(command)) {
            try {
                this.executeRunCommand(command);
            } catch (final SeMoDeException e) {
                System.err.println(e);
            }
            this.printRunCommandUsage();
            command = scanner.nextLine();
        }
    }

    private void printRunCommandUsage() {
        System.out.println();
        System.out.println("Please type in a command or \"exit\".");
        System.out.println("Benchmarking Options:");
        System.out.println(" (configBenchmark)     Alter/Specify the current configuration");
        System.out.println(" (deployBenchmark)     Starts the deployment");
        System.out.println(" (executeBenchmark)    Executes the benchmark");
        System.out.println(" (fetchBenchmark)      Fetch the benchmark data");
        System.out.println(" (undeployBenchmark)   Undeploying the current cloud functions");
        System.out.println("Simulation Options:");
        System.out.println(" (configCalibration)   Perform a calibration (linpack)");
        System.out.println(" (deployCalibration)   Starts the deployment (optional) and the configured calibration");
        System.out.println(" (undeployCalibration) Undeploys the calibration");
        System.out.println(" (mapping)             Computes the mapping between two calibrations");
        System.out.println(" (run)                 Run container based on calibration");
        System.out.println("Other Options:");
        System.out.println(" (status)              Get the current configuration");
        System.out.println(" (exit)                Terminate the program");
    }

    private void loadOrInitSetup(final String name) throws SeMoDeException {
        final PipelineSetup setup = new PipelineSetup(name);
        this.controller = new PipelineSetupController(setup);
        if (setup.setupAlreadyExists()) {
            this.controller.load();
        } else {
            this.controller.init();
        }

        System.out.println("Successfully loaded benchmark setup \"" + setup.name + "\"");
    }

    private void executeRunCommand(final String command) throws SeMoDeException {
        switch (command) {

            // benchmark options
            case "configBenchmark":
                this.controller.configureBenchmarkSetup();
                break;
            case "deployBenchmark":
                this.controller.deployFunctions();
                break;
            case "executeBenchmark":
                this.controller.executeBenchmark();
                break;
            case "fetchBenchmark":
                this.controller.fetchBenchmarkData();
                break;
            case "undeployBenchmark":
                this.controller.undeployBenchmark();
                break;
            // calibration options
            case "configCalibration":
                this.controller.configureCalibration();
                break;
            case "startCalibration":
                try {
                    this.controller.startCalibration();
                } catch (final SeMoDeException e) {
                    // store current pipeline data before exiting
                    this.controller.savePipelineSetup();
                    throw e;
                }
                break;
            case "undeployCalibration":
                this.controller.undeployCalibration();
                break;
            case "mappging":
                // TODO
                break;
            case "run":
                // TODO
                break;
            // other program options
            case "status":
                this.controller.printPipelineSetupStatus();
                break;
            default:
                throw new SeMoDeException(
                        "The command " + command + " is not available. Check your spelling or open an Issue on github.",
                        new NotImplementedException(""));
        }

        // save after each operation
        this.controller.savePipelineSetup();
    }
}
