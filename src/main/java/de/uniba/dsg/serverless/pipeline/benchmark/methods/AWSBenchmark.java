package de.uniba.dsg.serverless.pipeline.benchmark.methods;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.benchmark.provider.LogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.provider.aws.AWSClient;
import de.uniba.dsg.serverless.pipeline.benchmark.provider.aws.AWSLogHandler;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AWSBenchmark implements BenchmarkMethods {

    private final String setupName;
    private final AWSBenchmarkConfig awsBenchmarkConfig;
    private final AWSClient awsClient;

    public AWSBenchmark(final String setupName, final AWSBenchmarkConfig awsBenchmarkConfig) throws SeMoDeException {
        this.setupName = setupName + "_benchmark";
        this.awsBenchmarkConfig = awsBenchmarkConfig;
        this.awsClient = new AWSClient(awsBenchmarkConfig.getRegion());
    }

    @Override
    public List<Pair<String, Integer>> generateFunctionNames() {
        final List<Pair<String, Integer>> functionConfigs = new ArrayList<>();
        for (final Integer memorySize : this.awsBenchmarkConfig.getMemorySizeList()) {
            for (final String deploymentSize : this.awsBenchmarkConfig.getDeploymentSizes()) {
                String functionName = this.setupName + "_" + memorySize + "_" + deploymentSize;
                functionConfigs.add(new ImmutablePair<>(functionName, memorySize));
            }
        }
        return functionConfigs;
    }

    @Override
    public List<String> getUrlEndpointsOnPlatform() {
        final List<String> urlEndpoints = new ArrayList<>();
        for (final Pair<String, Integer> functionName : this.generateFunctionNames()) {
            urlEndpoints.add(this.awsBenchmarkConfig.getTargetUrl() + "/" + functionName.getLeft());
        }
        return urlEndpoints;
    }

    @Override
    public Map<String, String> getHeaderParameter() {
        final Map<String, String> headerParameters = new HashMap<>();
        headerParameters.put("x-api-key", this.awsBenchmarkConfig.getApiKey());
        return headerParameters;
    }

    @Override
    public String getPlatform() {
        return CalibrationPlatform.AWS.getText();
    }

    /**
     * All values of the deploymentInternals are altered together, therefore a single check is sufficient.
     */
    @Override
    public boolean isInitialized() {
        final String restApiId = this.awsBenchmarkConfig.getApiKeyId();
        if (restApiId == null || "".equals(restApiId)) {
            return false;
        }
        return true;
    }

    @Override
    public List<PerformanceData> getPerformanceDataFromPlatform(final LocalDateTime startTime, final LocalDateTime endTime) throws SeMoDeException {

        List<PerformanceData> performanceDataList = new ArrayList<>();
        for (final Pair<String, Integer> functionName : this.generateFunctionNames()) {
            final LogHandler logHandler = new AWSLogHandler(this.awsBenchmarkConfig.getRegion(), "/aws/lambda/" + functionName.getLeft(), startTime, endTime);

            log.info("Fetch data for " + functionName.getLeft() + " from " + startTime + " to " + endTime);
            performanceDataList.addAll(logHandler.getPerformanceData());
        }

        return performanceDataList;
    }

    @Override
    public void deploy() {
        this.awsClient.deployFunctions(this.setupName, this.generateFunctionNames(), this.awsBenchmarkConfig);
    }

    @Override
    public void undeploy() {
        this.awsClient.removeAllDeployedResources(this.generateFunctionNames(), this.awsBenchmarkConfig);
        this.awsBenchmarkConfig.resetConfig();
    }
}
