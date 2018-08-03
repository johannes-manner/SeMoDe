package de.uniba.dsg.serverless.cli;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;

public class PipelineSetupUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(PipelineSetupUtility.class.getName());

	public PipelineSetupUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		if (args.size() < 2) {
			logger.fatal("Wrong parameter size: " + "\nUSAGE: COMMAND SETUP_NAME");
			return;
		}
		BenchmarkSetup setup;
		try {
			setup = getBenchMarkSetup(args.get(0));
		} catch (SeMoDeException e) {
			logger.fatal(e);
			return;
		}
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		controller.loadConfig();
		// ask for changes / actions
	}

	private BenchmarkSetup getBenchMarkSetup(String command) throws SeMoDeException {
		// TODO is there a nicer way of switching between commands? maybe similar to
		// UtilityFactory
		switch (command) {
		case "init":
			return BenchmarkSetup.initialize(command);
		case "load":
			return BenchmarkSetup.load(command);
		default:
			throw new SeMoDeException(
					"The command " + command + " is not available. Check your spelling or open an Issue on github.");
		}
	}

}
