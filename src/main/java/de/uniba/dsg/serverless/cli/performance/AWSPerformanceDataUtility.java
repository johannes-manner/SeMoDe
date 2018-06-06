package de.uniba.dsg.serverless.cli.performance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.provider.aws.AWSLogHandler;

public final class AWSPerformanceDataUtility extends CustomUtility implements PerformanceData{

	private static final Logger logger = LogManager.getLogger(AWSPerformanceDataUtility.class.getName());

	public AWSPerformanceDataUtility(String name) {
		super(name);
	}

	public void start(List<String> args) {

		if (args.size() < 4) {
			logger.fatal("Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - " + "\n(2) LogGroupName "
					+ "\n(3) Start time filter of performance data" + "\n(4) End time filter of performance data"
					+ "\n(5) Optional - REST calls file");
			return;
		}

		String region = args.get(0);
		String logGroupName = args.get(1);
		String startTimeString = args.get(2);
		String endTimeString = args.get(3);

		try {
			this.validateStartEnd(startTimeString, endTimeString);
			LocalDateTime startTime = this.parseTime(startTimeString);
			LocalDateTime endTime = this.parseTime(endTimeString);
			AWSLogHandler logHandler = new AWSLogHandler(region, logGroupName, startTime, endTime);
			Optional<String> restFile;
			if (args.size() == 5) {
				restFile = Optional.of(args.get(4));
			}else {
				restFile = Optional.empty();
			}
			
			this.writePerformanceDataToFile(logHandler, logGroupName.substring("/aws/lambda/".length()), restFile);
			
		} catch (SeMoDeException e) {
			logger.fatal(e.getMessage() + "Cause: "
					+ (e.getCause() == null ? "No further cause!" : e.getCause().getMessage()));
		}
	}
}
