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
				printRunCommandUsage();
			}
			command = scanner.nextLine();
		}
	}

	private void printRunCommandUsage() {
		System.out.println("Please type in a command or \"exit\".");
	}

	private void executeSetupCommand(String command, String name) throws SeMoDeException {
		// TODO is there a nicer way of switching between commands? maybe similar to
		// UtilityFactory
		BenchmarkSetup setup = new BenchmarkSetup(name);
		controller = new BenchmarkSetupController(setup);
		switch (command) {
		case "init":
			controller.initBenchmark();
			break;
		case "load":
			controller.loadBenchmark();
			break;
		default:
			throw new SeMoDeException(
					"The command " + command + " is not available. Check your spelling or open an Issue on github.");
		}
		logger.info("Successfully loaded benchmark setup \"" + setup.name + "\"");
	}

	private void executeRunCommand(String command) throws SeMoDeException {
		switch (command) {
		case "config":
			controller.configureBenchmarkSetup();
			break;
		default:
			throw new SeMoDeException(
					"The command " + command + " is not available. Check your spelling or open an Issue on github.",
					new NotImplementedException(""));
		}
	}
}