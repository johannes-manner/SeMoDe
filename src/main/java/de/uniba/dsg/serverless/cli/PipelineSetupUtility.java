package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.pipeline.controller.PipelineSetupController;
import de.uniba.dsg.serverless.pipeline.model.PipelineSetup;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Scanner;

public class PipelineSetupUtility extends CustomUtility {

    private static final Scanner scanner = new Scanner(System.in);

    private PipelineSetupController controller;
    private FileLogger logger;

    public PipelineSetupUtility(final String name) {
        super(name);
        this.logger = ArgumentProcessor.logger;
    }

    @Override
    public void start(final List<String> args) {
        if (args.size() < 1) {
            this.logger.warning("Wrong parameter size: " + "\nUSAGE: SETUP_NAME");
            return;
        }
        try {
            this.loadOrInitSetup(args.get(0));
        } catch (final SeMoDeException e) {
            this.logger.warning(e.getMessage());
            return;
        }

        // change to the pipeline logger
        this.logger = this.controller.getPipelineLogger();
        this.printRunCommandUsage();
        String command = scanner.nextLine();
        this.logger.info("Entered Command: " + command);
        while (!"exit".equals(command)) {
            try {
                this.executeRunCommand(command);
            } catch (final SeMoDeException e) {
                this.logger.warning(e.getMessage());
            }
            this.printRunCommandUsage();
            command = scanner.nextLine();
            this.logger.info("Entered Command: " + command);
        }
    }

    /**
     * Some sort of construction method since the constructor is needed for
     * the utility creation for having the utility included in the {@link UtilityFactory}.
     *
     * @param name
     * @throws SeMoDeException
     */
    private void loadOrInitSetup(final String name) throws SeMoDeException {
        final PipelineSetup setup = new PipelineSetup(name);
        this.controller = new PipelineSetupController(setup);
        if (setup.setupAlreadyExists()) {
            this.controller.load();
        } else {
            this.controller.init();
        }

        this.logger.info("Successfully loaded benchmark setup \"" + setup.name + "\"");
    }

    private void printRunCommandUsage() {
        this.logger.info("");
        this.logger.info("Please type in a command or \"exit\".");
        this.logger.info("Benchmarking Options:");
        this.logger.info(" (configBenchmark)     Alter/Specify the current configuration");
        this.logger.info(" (deployBenchmark)     Starts the deployment");
        this.logger.info(" (executeBenchmark)    Executes the benchmark");
        this.logger.info(" (fetchBenchmark)      Fetch the benchmark data");
        this.logger.info(" (undeployBenchmark)   Undeploying the current cloud functions");
        this.logger.info("Simulation Options:");
        this.logger.info(" (configCalibration)   Perform a calibration (linpack)");
        this.logger.info(" (deployCalibration)   Starts the deployment (optional)");
        this.logger.info(" (startCalibration)    Starts the calibration");
        this.logger.info(" (undeployCalibration) Undeploys the calibration");
        this.logger.info(" (mapping)             Computes the mapping between two calibrations");
        this.logger.info(" (run)                 Run container based on calibration");
        this.logger.info("Other Options:");
        this.logger.info(" (status)              Get the current configuration");
        this.logger.info(" (exit)                Terminate the program");
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
            case "deployCalibration":
                try {
                    this.controller.deployCalibration();
                } catch (final SeMoDeException e) {
                    // store current pipeline data before exiting
                    this.controller.savePipelineSetup();
                    throw e;
                }
                break;
            case "startCalibration":
                this.controller.startCalibration();
                break;
            case "undeployCalibration":
                this.controller.undeployCalibration();
                break;
            case "mapping":
                this.controller.computeMapping();
                break;
            case "run":
                this.controller.runLocalContainer();
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
