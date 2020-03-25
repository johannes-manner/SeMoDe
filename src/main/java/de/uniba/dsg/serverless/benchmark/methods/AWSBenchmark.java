package de.uniba.dsg.serverless.benchmark.methods;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.benchmark.data.PerformanceDataWriter;
import de.uniba.dsg.serverless.benchmark.logs.LogHandler;
import de.uniba.dsg.serverless.benchmark.logs.aws.AWSLogHandler;
import de.uniba.dsg.serverless.calibration.aws.AWSClient;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import de.uniba.dsg.serverless.util.BenchmarkingRESTAnalyzer;
import de.uniba.dsg.serverless.util.FileLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSBenchmark implements BenchmarkMethods {

    private static final FileLogger logger = ArgumentProcessor.logger;

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
        for (final Integer memorySize : this.awsBenchmarkConfig.functionConfig.memorySizes) {
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
        return SupportedPlatform.AWS.getText();
    }

    /**
     * All values of the deploymentInternals are altered together,
     * therefore a single check is sufficient.
     *
     * @return
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
    public void writePerformanceDataToFile(final Path path, final LocalDateTime startTime, final LocalDateTime endTime) throws SeMoDeException {
        final PerformanceDataWriter writer = new PerformanceDataWriter();

        for (final Pair<String, Integer> functionName : this.generateFunctionNames()) {
            final LogHandler logHandler = new AWSLogHandler(this.awsBenchmarkConfig.functionConfig.region, "/aws/lambda/" + functionName.getLeft(), startTime, endTime);
            // TODO change REST FILE
            logger.info("Fetch data for " + functionName.getLeft() + " from " + startTime + " to " + endTime);
            final BenchmarkingRESTAnalyzer restHandler = new BenchmarkingRESTAnalyzer(SupportedPlatform.AWS.getText(), Paths.get(path.toString(), "execution.log"));
            writer.writePerformanceDataToFile(path.resolve(SupportedPlatform.AWS.getText() + "_" + functionName.getLeft() + ".csv"), logHandler, restHandler);
        }
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
