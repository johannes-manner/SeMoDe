package de.uniba.dsg.serverless;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.cli.UtilityFactory;

public class ArgumentProcessor {

	public static void main(String[] args) {
		// defining dynamic properties for log4j2
		System.setProperty("logFilename", "benchmarking_" + new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date()));
		System.setProperty("CSV_SEPARATOR", ";");
		System.setProperty("DATE_TIME_FORMAT", "yyyy-MM-dd HH:mm:ss.SSS");

		if (args != null && args.length > 2 && args[0].equals("benchmark")) {
			System.setProperty("functionName", args[1]);
		}

		Logger logger = LogManager.getLogger(ArgumentProcessor.class.getName());

		if (args == null || args.length < 2) {
			logger.warn("Please specify cli arguments. Program exited.");
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
			logger.warn("The utility mechanism is not supported. Select a supported one");
		}
	}
}
