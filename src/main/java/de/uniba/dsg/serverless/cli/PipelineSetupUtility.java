package de.uniba.dsg.serverless.cli;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.Pipeline;

public class PipelineSetupUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(PipelineSetupUtility.class.getName());

	public PipelineSetupUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		// TODO is there a nicer way of switching between commands? maybe similar to
		// UtilityFactory
		String command = args.get(0);
		try {
			switch (command) {
			case "init":
				if (args.size() < 2) {
					logger.fatal("Wrong parameter size: " + "\nUSAGE: init SETUP_NAME");
					return;
				}
				new Pipeline(args.get(1));
				break;
			default:
				throw new IllegalArgumentException("The command " + command
						+ " is not available. Check your spelling or open an Issue on github.");
			}
		} catch (SeMoDeException e) {
			logger.fatal(e);
			return;
		}
	}

}
