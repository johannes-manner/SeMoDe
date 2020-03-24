package de.uniba.dsg.serverless.benchmark.aws;

import de.uniba.dsg.serverless.benchmark.BenchmarkMethods;
import de.uniba.dsg.serverless.calibration.aws.AWSClient;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

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
    public void deploy() {
        this.awsClient.deployFunctions(this.setupName, this.generateFunctionNames(), this.awsBenchmarkConfig.functionConfig, this.awsBenchmarkConfig.getDeploymentInternals());
    }

    @Override
    public void undeploy() {
        this.awsClient.removeAllDeployedResources(this.generateFunctionNames(), this.awsBenchmarkConfig.deploymentInternals);
        this.awsBenchmarkConfig.resetConfig();
    }
}
