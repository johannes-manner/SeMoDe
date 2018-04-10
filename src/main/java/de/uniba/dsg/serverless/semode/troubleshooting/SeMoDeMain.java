package de.uniba.dsg.serverless.semode.troubleshooting;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.semode.troubleshooting.service.AWSLogHandler;

/**
 * 
 * This is the main class for starting the analysis process and generating test classes.
 * The command line arguments are as follows: <br/>
 *  <b>(1)</b>  Region, e.g. \"eu-west-1\" <br/>
 *  <b>(2)</b>  LogGroupName <br/>
 *  <b>(3)</b>  search string, e.g. "Exception" to tackle java exception <br/>
 *  
 * 
 * @author Johannes Manner
 * 
 * @version 1.0
 *
 */
public final class SeMoDeMain {
	
	private static final Logger logger = Logger.getLogger(SeMoDeMain.class.getName());
	
	public static void main(String[] args) {
		
		if(args.length < 3) {
			SeMoDeMain.logger.log(Level.SEVERE, "Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - "
																		+ "\n(2) LogGroupName "
																		+ "\n(3) search string, e.g. \"exception\" to tackle java exception");
			return;
		}
		
		String region = args[0];
		String logGroupName = args[1];
		String searchString = args[2];
		
		logger.log(Level.INFO, "Region: " + region + "\tLogGroupName: " + logGroupName + "\tSearch string: " + searchString);
		logger.log(Level.INFO, System.getProperty("java.class.path"));
		new AWSLogHandler(region, logGroupName).startAnalzying(searchString);
	}
}
