package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;

public class PipelineSetupUtility extends CustomUtility {

    public static final Scanner scanner = new Scanner(System.in);
    private static final Logger logger = LogManager.getLogger(PipelineSetupUtility.class.getName());

    private BenchmarkSetupController controller;

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
            this.executeSetupCommand(args.get(0));
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
        System.out.println(" (status)     Get the current configuration");
        System.out.println(" (config)     Alter/Specify the current configuration");
        System.out.println(" (deploy)     Starts the deployment");
        System.out.println(" (endpoints)  Generate endpoints for benchmarking");
        System.out.println(" (commands)   Generate benchmarking commands in a bat-file");
        System.out.println(" (fetch)      Fetch log data from various platforms");
        System.out.println(" (undeploy)   Undeploying the current cloud functions");
        System.out.println(" (exit)       Terminate the program");
    }

    private void executeSetupCommand(final String name) throws SeMoDeException {
        final BenchmarkSetup setup = new BenchmarkSetup(name);

        if (setup.setupAlreadyExists()) {
            this.controller = BenchmarkSetupController.load(setup);
        } else {
            this.controller = BenchmarkSetupController.init(setup);
        }

        logger.info("Successfully loaded benchmark setup \"" + setup.name + "\"");
    }

    private void executeRunCommand(final String command) throws SeMoDeException {
        switch (command) {
            case "status":
                this.controller.printBenchmarkSetupStatus();
                break;
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
            default:
                throw new SeMoDeException(
                        "The command " + command + " is not available. Check your spelling or open an Issue on github.",
                        new NotImplementedException(""));
        }
    }
}
