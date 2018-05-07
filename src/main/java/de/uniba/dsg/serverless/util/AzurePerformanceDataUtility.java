package de.uniba.dsg.serverless.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.dsg.serverless.azure.AzureLogHandler;

public final class AzurePerformanceDataUtility extends CustomUtility {

	private static final Logger logger = Logger.getLogger(AzurePerformanceDataUtility.class.getName());

	public AzurePerformanceDataUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 3) {
			AzurePerformanceDataUtility.logger.log(Level.SEVERE, "Wrong parameter size: "
					+ "\n(1) Application ID " + "\n(2) API Key " + "\n(3) Function Name");
			return;
		}
		
		String applicationID = args.get(0);
		String apiKey = args.get(1);
		String functionName = args.get(2);

		String dateText = new SimpleDateFormat("MM-dd-HH-mm").format(new Date());
		String fileName = functionName + "-" + dateText + ".csv";

		new AzureLogHandler(applicationID, apiKey, functionName)
				.writePerformanceDataToFile(fileName);
	}
}
