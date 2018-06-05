package de.uniba.dsg.serverless.cli.performance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.provider.ibm.IBMLogHandler;

public class IBMOpenWhiskPerformanceDataUtility extends CustomUtility implements PerformanceData {

	public static final Logger logger = LogManager.getLogger(IBMOpenWhiskPerformanceDataUtility.class.getName());

	public IBMOpenWhiskPerformanceDataUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {

		if (args.size() < 5) {
			logger.fatal("Wrong parameter size: \n(1) OpenWhisk namespace \n(2) Function name \n(3) Authorization token"
					+ "\n(4) Start time filter of performance data" + "\n(5) End time filter of performance data"
					+ "\n(6) Optional - REST calls file");
			return;
		}

		String namespace = args.get(0);
		String functionName = args.get(1);
		String authorizationToken = args.get(2);
		String startTimeString = args.get(3);
		String endTimeString = args.get(4);

		try {

			this.validateStartEnd(startTimeString, endTimeString);

			LocalDateTime startTime = this.parseTime(startTimeString);
			LocalDateTime endTime = this.parseTime(endTimeString);

			IBMLogHandler logHandler = new IBMLogHandler(namespace, functionName, authorizationToken, startTime, endTime);
			
			// if a benchmarking file is selected
			Optional<String> restFile;
			if (args.size() == 6) {
				restFile = Optional.of(args.get(5));
			} else {
				restFile = Optional.empty();
			}
			
			this.writePerformanceDataToFile(logHandler, functionName, restFile);
		} catch (SeMoDeException e) {
			logger.fatal(e.getMessage() + "Cause: " + e.getCause() == null ? "No further cause!"
					: e.getCause().getMessage());
		}
	}
}
