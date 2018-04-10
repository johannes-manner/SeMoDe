package de.uniba.dsg.serverless.semode.performance;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.semode.troubleshooting.service.AWSLogHandler;

public class PerformanceDataMain {
	
	private static final Logger logger = Logger.getLogger(PerformanceDataMain.class.getName());
	
	public static void main(String[] args) {
		
		if(args.length < 2){
			PerformanceDataMain.logger.log(Level.SEVERE, "Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - "
					+ "\n(2) LogGroupName ");
			return;
		}
		
		String region = args[0];
		String logGroupName = args[1];
		
		String fileName = logGroupName.substring("/aws/lambda/".length()) + (System.currentTimeMillis() % 1000) + ".csv";
		
		new AWSLogHandler(region,logGroupName).writePerformanceDataToFile(fileName);
	}
}
