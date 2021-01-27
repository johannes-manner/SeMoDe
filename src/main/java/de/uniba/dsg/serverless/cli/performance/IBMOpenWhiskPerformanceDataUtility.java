package de.uniba.dsg.serverless.cli.performance;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.pipeline.benchmark.log.ibm.IBMLogHandler;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Deprecated
@Slf4j
public class IBMOpenWhiskPerformanceDataUtility extends CustomUtility {

    public IBMOpenWhiskPerformanceDataUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {

        if (args.size() < 5) {
            log.warn("Wrong parameter size: \n(1) OpenWhisk namespace \n(2) Function name \n(3) Authorization token"
                    + "\n(4) Start time filter of performance data" + "\n(5) End time filter of performance data"
                    + "\n(6) Optional - REST calls file");
            return;
        }

        final String namespace = args.get(0);
        final String functionName = args.get(1);
        final String authorizationToken = args.get(2);
        final String startTimeString = args.get(3);
        final String endTimeString = args.get(4);

        try {

            this.validateStartEnd(startTimeString, endTimeString);

            final LocalDateTime startTime = this.parseTime(startTimeString);
            final LocalDateTime endTime = this.parseTime(endTimeString);

            final IBMLogHandler logHandler = new IBMLogHandler(namespace, functionName, authorizationToken, startTime, endTime);

            // if a benchmarking file is selected
            final Optional<String> restFile;
            if (args.size() == 6) {
                restFile = Optional.of(args.get(5));
            } else {
                restFile = Optional.empty();
            }

//            this.fetcher.getPerformanceDataFromPlatform("ibm", logHandler, functionName, restFile);
        } catch (final SeMoDeException e) {
            log.warn(e.getMessage() + "Cause: " + (e.getCause() == null ? "No further cause!"
                    : e.getCause().getMessage()));
        }
    }
}
