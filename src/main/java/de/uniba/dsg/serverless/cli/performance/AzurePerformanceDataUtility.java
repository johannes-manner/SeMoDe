package de.uniba.dsg.serverless.cli.performance;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.pipeline.benchmark.log.azure.AzureLogHandler;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Deprecated
@Slf4j
public final class AzurePerformanceDataUtility extends CustomUtility {

    public AzurePerformanceDataUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {

        if (args.size() < 6) {
            log.warn("Wrong parameter size: " + "\n(1) Application ID " + "\n(2) API Key " + "\n(3) Service Name"
                    + "\n(4) Function Name" + "\n(5) Start time filter of performance data"
                    + "\n(6) End time filter of performance data" + "\n(7) Optional - REST calls file");
            return;
        }

        final String applicationID = args.get(0);
        final String apiKey = args.get(1);
        final String serviceName = args.get(2);
        final String functionName = args.get(3);
        final String startTimeString = args.get(4);
        final String endTimeString = args.get(5);

        try {
            this.validateStartEnd(startTimeString, endTimeString);
            final LocalDateTime startTime = this.parseTime(startTimeString);
            final LocalDateTime endTime = this.parseTime(endTimeString);

            final AzureLogHandler azureLogHandler = new AzureLogHandler(applicationID, apiKey, functionName, startTime,
                    endTime);

            final Optional<String> restFile;
            if (args.size() == 7) {
                restFile = Optional.of(args.get(6));
            } else {
                restFile = Optional.empty();
            }

//            this.fetcher.getPerformanceDataFromPlatform("azure", azureLogHandler, serviceName, restFile);
        } catch (final SeMoDeException e) {
            log.warn(e.getMessage() + "Cause: " + (e.getCause() == null ? "No further cause!" : e.getCause().getMessage()));
        }
    }
}
