package de.uniba.dsg.serverless.util;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.aws.AWSLogHandler;

public final class PerformanceDataUtility extends CustomUtility{
	
	private static final Logger logger = Logger.getLogger(PerformanceDataUtility.class.getName());

	public PerformanceDataUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 2) {
			PerformanceDataUtility.logger.log(Level.SEVERE,
					"Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - " + "\n(2) LogGroupName ");
			return;
		}

		String region = args.get(0);
		String logGroupName = args.get(1);

		String fileName = logGroupName.substring("/aws/lambda/".length()) + (System.currentTimeMillis() % 1000)
				+ ".csv";

		new AWSLogHandler(region, logGroupName).writePerformanceDataToFile(fileName);
	}
}
