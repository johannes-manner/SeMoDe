package de.uniba.dsg.serverless.pipeline.benchmark.methods;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniba.dsg.serverless.pipeline.benchmark.log.LogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.log.aws.AWSLogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.sdk.AWSClient;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class AWSBenchmark implements BenchmarkMethods {

    private final String setupName;
    private final AWSBenchmarkConfig awsBenchmarkConfig;
    private final AWSClient awsClient;
    private final String platformPrefix;

    public AWSBenchmark(final String setupName, final AWSBenchmarkConfig awsBenchmarkConfig) throws SeMoDeException {
        this.setupName = setupName + "_benchmark";
        this.awsBenchmarkConfig = awsBenchmarkConfig;
        this.awsClient = new AWSClient(awsBenchmarkConfig.functionConfig.region);
        this.platformPrefix = this.setupName + "_";
    }

    @Override
    public List<Pair<String, Integer>> generateFunctionNames() {
        final List<Pair<String, Integer>> functionConfigs = new ArrayList<>();
        for (final Integer memorySize : this.awsBenchmarkConfig.functionConfig.getMemorySizeList()) {
            functionConfigs.add(new ImmutablePair<>(this.platformPrefix + memorySize, memorySize));
        }
        return functionConfigs;
    }

    @Override
    public List<String> getUrlEndpointsOnPlatform() {
        final List<String> urlEndpoints = new ArrayList<>();
        for (final Pair<String, Integer> functionName : this.generateFunctionNames()) {
            urlEndpoints.add(this.awsBenchmarkConfig.functionConfig.targetUrl + "/" + functionName.getLeft());
        }
        return urlEndpoints;
    }

    @Override
    public Map<String, String> getHeaderParameter() {
        final Map<String, String> headerParameters = new HashMap<>();
        headerParameters.put("x-api-key", this.awsBenchmarkConfig.functionConfig.apiKey);
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
        final String restApiId = this.awsBenchmarkConfig.deploymentInternals.apiKeyId;
        if (restApiId == null || "".equals(restApiId)) {
            return false;
        }
        return true;
    }

    @Override
    public List<PerformanceData> getPerformanceDataFromPlatform(final LocalDateTime startTime, final LocalDateTime endTime) throws SeMoDeException {

        List<PerformanceData> performanceDataList = new ArrayList<>();
        for (final Pair<String, Integer> functionName : this.generateFunctionNames()) {
            final LogHandler logHandler = new AWSLogHandler(this.awsBenchmarkConfig.functionConfig.region, "/aws/lambda/" + functionName.getLeft(), startTime, endTime);

            log.info("Fetch data for " + functionName.getLeft() + " from " + startTime + " to " + endTime);
            performanceDataList.addAll(logHandler.getPerformanceData());
        }

        return performanceDataList;
    }

    @Override
    public void deploy() {
        this.awsClient.deployFunctions(this.setupName, this.generateFunctionNames(), this.awsBenchmarkConfig.functionConfig, this.awsBenchmarkConfig.getDeploymentInternals());
    }

    @Override
    public void undeploy() {
        this.awsClient.removeAllDeployedResources(this.generateFunctionNames(), this.awsBenchmarkConfig.deploymentInternals);
        this.awsBenchmarkConfig.resetConfig();
    }
}
