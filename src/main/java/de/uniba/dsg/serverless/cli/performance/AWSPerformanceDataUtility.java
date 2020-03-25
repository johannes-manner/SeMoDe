package de.uniba.dsg.serverless.cli.performance;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.benchmark.data.PerformanceDataWriter;
import de.uniba.dsg.serverless.benchmark.logs.aws.AWSLogHandler;
import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.util.FileLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Deprecated
public final class AWSPerformanceDataUtility extends CustomUtility {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private final PerformanceDataWriter fetcher;

    public AWSPerformanceDataUtility(final String name) {
        super(name);
        this.fetcher = new PerformanceDataWriter();
    }

    @Override
    public void start(final List<String> args) {

        if (args.size() < 4) {
            logger.warning("Wrong parameter size: \n(1) Region, e.g. \"eu-west-1\" - " + "\n(2) LogGroupName "
                    + "\n(3) Start time filter of performance data" + "\n(4) End time filter of performance data"
                    + "\n(5) Optional - REST calls file");
            return;
        }

        final String region = args.get(0);
        final String logGroupName = args.get(1);
        final String startTimeString = args.get(2);
        final String endTimeString = args.get(3);

        try {
            this.validateStartEnd(startTimeString, endTimeString);
            final LocalDateTime startTime = this.parseTime(startTimeString);
            final LocalDateTime endTime = this.parseTime(endTimeString);
            final AWSLogHandler logHandler = new AWSLogHandler(region, logGroupName, startTime, endTime);
            final Optional<String> restFile;
            if (args.size() == 5) {
                restFile = Optional.of(args.get(4));
            } else {
                restFile = Optional.empty();
            }

//            this.fetcher.writePerformanceDataToFile("aws", logHandler, logGroupName.substring("/aws/lambda/".length()), restFile);

        } catch (final SeMoDeException e) {
            logger.warning(e.getMessage() + "Cause: "
                    + (e.getCause() == null ? "No further cause!" : e.getCause().getMessage()));
        }
    }
}
