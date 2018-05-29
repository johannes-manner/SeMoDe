package de.uniba.dsg.serverless.cli;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.aws.AWSLogHandler;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;
import de.uniba.dsg.serverless.util.BenchmarkingRESTAnalyzer;

public final class AWSPerformanceDataUtility extends CustomUtility {

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
			String fileName = this.generateFileName(logGroupName.substring("/aws/lambda/".length()));
			
			LocalDateTime startTime = this.parseTime(startTimeString);
			LocalDateTime endTime = this.parseTime(endTimeString);

			List<Map<String, WritableEvent>> elementList = new ArrayList<>();
			AWSLogHandler logHandler = new AWSLogHandler(region, logGroupName, startTime, endTime);

			elementList.add(logHandler.getPerformanceData());
			
			// if a benchmarking file is selected
			if (args.size() == 5) {
				BenchmarkingRESTAnalyzer restAnalyzer = new BenchmarkingRESTAnalyzer(Paths.get(args.get(4)));
				elementList.add(restAnalyzer.extractRESTEvents());
			}
			
			logHandler.writePerformanceDataToFile(fileName, elementList);

		} catch (SeMoDeException e) {
			logger.fatal(e.getMessage());
		}
	}
}
