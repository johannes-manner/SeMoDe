package de.uniba.dsg.serverless.cli;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.aws.AWSLogHandler;

public final class AWSPerformanceDataUtility extends CustomUtility{
	
	private static final Logger logger = LogManager.getLogger(AWSPerformanceDataUtility.class.getName());

	public AWSPerformanceDataUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 2) {
			logger.fatal("Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - " + "\n(2) LogGroupName ");
			return;
		}

		String region = args.get(0);
		String logGroupName = args.get(1);

		String dateText = new SimpleDateFormat("MM-dd-HH-mm").format(new Date());
		String fileName = logGroupName.substring("/aws/lambda/".length()) + "-" + dateText + ".csv";

		new AWSLogHandler(region, logGroupName).writePerformanceDataToFile(fileName);
	}
}
