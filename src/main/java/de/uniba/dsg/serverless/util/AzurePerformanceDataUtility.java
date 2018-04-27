package de.uniba.dsg.serverless.util;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.azure.AzureLogHandler;

public final class AzurePerformanceDataUtility extends CustomUtility{
	
	private static final Logger logger = Logger.getLogger(AzurePerformanceDataUtility.class.getName());

	public AzurePerformanceDataUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 4) {
			AzurePerformanceDataUtility.logger.log(Level.SEVERE,
					"Wrong parameter size: "
							+ "\n(1) Account Name " 
							+ "\n(2) Account Key "
							+ "\n(3) Share"
							+ "n(4) Function Name");
			return;
		}

		String accountName = args.get(0);
		String accountKey = args.get(1);
		String shareName = args.get(2);
		String functionName = args.get(3);
		
		new AzureLogHandler(accountName, accountKey, shareName, functionName).downloadFiles();
	}
}
