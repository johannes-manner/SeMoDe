package de.uniba.dsg.serverless.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.aws.AWSLogHandler;

/**
 * 
 * This is the main class for starting the analysis process and generating test
 * classes. The command line arguments are as follows: <br/>
 * <b>(1)</b> Region, e.g. \"eu-west-1\" <br/>
 * <b>(2)</b> LogGroupName <br/>
 * <b>(3)</b> search string, e.g. "Exception" to tackle java exception <br/>
 *
 */
public final class SeMoDeUtility extends CustomUtility {
	
	private static final Logger logger = LogManager.getLogger(SeMoDeUtility.class.getName());

	public SeMoDeUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 3) {
			logger.fatal("Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - "
					+ "\n(2) LogGroupName " + "\n(3) search string, e.g. \"exception\" to tackle java exception");
			return;
		}

		String region = args.get(0);
		String logGroupName = args.get(1);
		String searchString = args.get(2);

		logger.info("Region: " + region + "\tLogGroupName: " + logGroupName + "\tSearch string: " + searchString);
		new AWSLogHandler(region, logGroupName).startAnalzying(searchString);
	}
}
