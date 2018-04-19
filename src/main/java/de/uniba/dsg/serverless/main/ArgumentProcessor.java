package de.uniba.dsg.serverless.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.util.CustomUtility;
import de.uniba.dsg.serverless.util.UtilityFactory;

public class ArgumentProcessor {

	private static final Logger logger = Logger.getLogger(ArgumentProcessor.class.getName());

	public static void main(String[] args) {
		if (args == null || args.length < 2) {
			logger.warning("Please specify cli arguments. Program exited.");
			return;
		}

		List<String> argumentList = new ArrayList<>();
		for (int i = 1; i < args.length; i++) {
			argumentList.add(args[i]);
		}

		Optional<CustomUtility> utility = UtilityFactory.getUtilityClass(args[0]);
		if (utility.isPresent()) {
			utility.get().start(argumentList);
		} else {
			logger.warning("The utility mechanism is not supported. Select a supported one");
		}
	}
}
