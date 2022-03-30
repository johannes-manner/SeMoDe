package de.uniba.dsg.serverless.pipeline.benchmark.methods;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.model.config.openfaas.OpenFaasBenchmarkConfig;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OpenFaaSBenchmark {

    private OpenFaasBenchmarkConfig openFaasBenchmarkConfig;

    public OpenFaaSBenchmark(OpenFaasBenchmarkConfig openFaasBenchmarkConfig) {
        this.openFaasBenchmarkConfig = openFaasBenchmarkConfig;
    }

    public List<PerformanceData> getPerformanceDataFromPlatform() {

        List<PerformanceData> performanceDataList = new ArrayList<>();
        for (String quota : this.openFaasBenchmarkConfig.getResourceSettings()) {
            for (int i = 0; i < this.openFaasBenchmarkConfig.getOpenFaaSNumberOfRuns(); i++) {
                String file = openFaasBenchmarkConfig.getOpenFaaSBaseUrl() + "-" + quota + "-" + i + ".log";
                WebTarget openFaasRequest = ClientBuilder.newClient().target(file);
                Response r = openFaasRequest.request()
                        .get();

                List<String> responseEntity = r.readEntity(List.class);
                int execTime = findFirstInteger(responseEntity.get(0));

                log.info("OpenFaaS execution time for " + quota + " run " + i + ": " + execTime);

                // currently the AWS values are responsible for naming
                // TODO change naming of memory size, maybe also remove some fields of the performance data class
                // now also cpu shares are used there
                PerformanceData performanceData = new PerformanceData(file,
                        file,
                        quota + "-" + i,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0,
                        execTime,
                        execTime,
                        Integer.valueOf(quota),
                        Integer.valueOf(quota));

                performanceDataList.add(performanceData);
            }
        }
        return performanceDataList;
    }

    private int findFirstInteger(String stringToSearch) {
        Pattern integerPattern = Pattern.compile("-?\\d+");
        Matcher matcher = integerPattern.matcher(stringToSearch);

        List<String> integerList = new ArrayList<>();
        while (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }

        return -1;
    }

}
