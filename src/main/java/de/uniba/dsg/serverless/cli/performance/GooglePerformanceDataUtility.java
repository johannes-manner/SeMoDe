package de.uniba.dsg.serverless.cli.performance;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.pipeline.benchmark.provider.google.GoogleLogHandler;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Deprecated
@Slf4j
public class GooglePerformanceDataUtility extends CustomUtility {

    public GooglePerformanceDataUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {

        if (args.size() < 3) {
            log.warn("Wrong parameter size: \n(1) Function Name"
                    + "\n(2) Start time filter of performance data" + "\n(3) End time filter of performance data"
                    + "\n(4) Optional - REST calls file");
            return;
        }

        final String functionName = args.get(0);
        final String startTimeString = args.get(1);
        final String endTimeString = args.get(2);

        try {

            this.validateStartEnd(startTimeString, endTimeString);

            final LocalDateTime startTime = this.parseTime(startTimeString);
            final LocalDateTime endTime = this.parseTime(endTimeString);

            final GoogleLogHandler logHandler = new GoogleLogHandler(functionName, startTime, endTime);

            // if a benchmarking file is selected
            final Optional<String> restFile;
            if (args.size() == 4) {
                restFile = Optional.of(args.get(3));
            } else {
                restFile = Optional.empty();
            }

//            this.fetcher.getPerformanceDataFromPlatform("google", logHandler, functionName, restFile);

        } catch (final SeMoDeException e) {
            log.warn(e.getMessage() + "Cause: " + (e.getCause() == null ? "No further cause!" : e.getCause().getMessage()));
        }
    }
}
