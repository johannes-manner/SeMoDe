package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.controller.PipelineSetupController;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;

public class PipelineSetupUtility extends CustomUtility {

    public static final Scanner scanner = new Scanner(System.in);
    private static final Logger logger = LogManager.getLogger(PipelineSetupUtility.class.getName());

    private PipelineSetupController controller;

    public PipelineSetupUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {
        if (args.size() < 1) {
            logger.fatal("Wrong parameter size: " + "\nUSAGE: SETUP_NAME");
            return;
        }
        try {
            this.loadOrInitSetup(args.get(0));
        } catch (final SeMoDeException e) {
            logger.fatal(e);
            return;
        }
        this.printRunCommandUsage();
        String command = scanner.nextLine();
        while (!"exit".equals(command)) {
            try {
                this.executeRunCommand(command);
            } catch (final SeMoDeException e) {
                logger.fatal(e);
            }
            this.printRunCommandUsage();
            command = scanner.nextLine();
        }
    }

    private void printRunCommandUsage() {
        System.out.println();
        System.out.println("Please type in a command or \"exit\".");
        System.out.println("Benchmarking Options:");
        System.out.println(" (config)             Alter/Specify the current configuration");
        System.out.println(" (deploy)             Starts the deployment");
        System.out.println(" (endpoints)          Generate endpoints for benchmarking");
        System.out.println(" (commands)           Generate benchmarking commands in a bat-file");
        System.out.println(" (fetch)              Fetch log data from various platforms");
        System.out.println(" (undeploy)           Undeploying the current cloud functions");
        System.out.println(" (benchmark)          Starts the whole benchmarking pipeline");
        System.out.println("Simulation Options:");
        System.out.println(" (calibrate)          Perform a calibration (linpack)");
        System.out.println(" (startCalibration)   Starts the deployment (optional) and the configured calibration");
        System.out.println(" (stopCalibration)    Undeploys the calibration");
        System.out.println(" (mapping)            Computes the mapping between two calibrations");
        System.out.println(" (run)                Run container based on calibration");
        System.out.println("Other Options:");
        System.out.println(" (status)             Get the current configuration");
        System.out.println(" (exit)               Terminate the program");
    }

    private void loadOrInitSetup(final String name) throws SeMoDeException {
        final PipelineSetup setup = new PipelineSetup(name);
        this.controller = new PipelineSetupController(setup);
        if (setup.setupAlreadyExists()) {
            this.controller.load();
        } else {
            this.controller.init();
        }

        logger.info("Successfully loaded benchmark setup \"" + setup.name + "\"");
    }

    private void executeRunCommand(final String command) throws SeMoDeException {
        switch (command) {

            // benchmark options
            case "config":
                this.controller.configureBenchmarkSetup();
                break;
            case "deploy":
                this.controller.prepareDeployment();
                break;
            case "endpoints":
                this.controller.generateEndpoints();
                break;
            case "commands":
                this.controller.generateBenchmarkingCommands();
                break;
            case "fetch":
                this.controller.fetchPerformanceData();
                break;
            case "undeploy":
                this.controller.undeploy();
                break;
            case "benchmark":
                // TODO automate as much as possible
                break;
            // calibration options
            case "calibrate":
                this.controller.generateCalibration();
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
            case "stopCalibration":
                this.controller.stopCalibration();
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
