package de.uniba.dsg.serverless.cli;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;

public class PipelineSetupUtility extends CustomUtility {

	public static final Scanner scanner = new Scanner(System.in);
	private static final Logger logger = LogManager.getLogger(PipelineSetupUtility.class.getName());

	private BenchmarkSetupController controller;

	public PipelineSetupUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		if (args.size() < 2) {
			logger.fatal("Wrong parameter size: " + "\nUSAGE: COMMAND SETUP_NAME");
			return;
		}
		try {
			executeSetupCommand(args.get(0), args.get(1));
		} catch (SeMoDeException e) {
			logger.fatal(e);
			return;
		}
		printRunCommandUsage();
		String command = scanner.nextLine();
		while (!"exit".equals(command)) {
			try {
				executeRunCommand(command);
			} catch (SeMoDeException e) {
				logger.fatal(e);
			}
			printRunCommandUsage();
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

	private void executeSetupCommand(String command, String name) throws SeMoDeException {
		BenchmarkSetup setup = new BenchmarkSetup(name);
		switch (command) {
		case "init":
			controller = BenchmarkSetupController.init(setup);
			break;
		case "load":
			controller = BenchmarkSetupController.load(setup);
			break;
		default:
			throw new SeMoDeException("The command " + command + " is not available. "
					+ "Check your spelling or open an Issue on github.");
		}
		logger.info("Successfully loaded benchmark setup \"" + setup.name + "\"");
	}

	private void executeRunCommand(String command) throws SeMoDeException {
		switch (command) {
		case "status":
			controller.printBenchmarkSetupStatus();
			break;
		case "config":
			controller.configureBenchmarkSetup();
			break;
		case "deploy":
			controller.prepareDeployment();
			break;
		case "endpoints":
			controller.generateEndpoints();
			break;
		case "commands":
			controller.generateBenchmarkingCommands();
			break;
		case "fetch":
			controller.fetchPerformanceData();
			break;
		case "undeploy":
			controller.undeploy();
			break;
		default:
			throw new SeMoDeException(
					"The command " + command + " is not available. Check your spelling or open an Issue on github.",
					new NotImplementedException(""));
		}
	}
}
