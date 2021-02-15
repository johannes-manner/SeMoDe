package de.uniba.dsg.serverless.pipeline.calibration.provider;

import de.uniba.dsg.serverless.pipeline.calibration.Calibration;
import de.uniba.dsg.serverless.pipeline.calibration.LinpackParser;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.sdk.AWSClient;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AWSCalibration implements CalibrationMethods {

    private final AWSCalibrationConfig calibrationConfig;
    private final AWSClient client;
    // composite
    private final Calibration calibration;

    // prefix to identify the function / functions and the api gateway url
    // suffix is the memory setting
    private final String platformPrefix;

    // used for CLI feature
    public AWSCalibration(final AWSCalibrationConfig calibrationConfig) throws SeMoDeException {
        this.calibration = new Calibration(calibrationConfig.getBenchmarkConfig().getRegion() + "_calibration", CalibrationPlatform.AWS);
        this.calibrationConfig = calibrationConfig;
        this.client = new AWSClient(this.calibrationConfig.getBenchmarkConfig().getRegion());
        this.platformPrefix = this.calibration.name + "_linpack_";
    }

    private List<Pair<String, Integer>> generateFunctionNames() {
        // create function names
        final List<Pair<String, Integer>> functionConfigs = new ArrayList<>();
        for (final Integer memorySize : this.calibrationConfig.getBenchmarkConfig().getMemorySizeList()) {
            functionConfigs.add(new ImmutablePair<>(this.platformPrefix + memorySize, memorySize));
        }
        return functionConfigs;
    }

    @Override
    public void undeployCalibration() {
        final List<Pair<String, Integer>> functionConfigs = this.generateFunctionNames();
        this.client.removeAllDeployedResources(functionConfigs, this.calibrationConfig.getBenchmarkConfig());
        this.calibrationConfig.resetConfig();
    }

    /**
     * Prior to the calibration, a decision is made by the user, if the linpack deployment should be executed. See also
     * {@link AWSClient#removeAllDeployedResources}, if a error occurred during deployment and remove all deployed
     * resources from the platform.
     */
    @Override
    public void deployCalibration() {
        if (Files.exists(this.calibration.calibrationFile)) {
            log.info("Provider calibration already performed.");
            return;
        }

        // deploy linpack
        final List<Pair<String, Integer>> functionConfigs = this.generateFunctionNames();
        this.client.deployFunctions(this.calibration.name, functionConfigs, this.calibrationConfig.getBenchmarkConfig());

    }

    @Override
    public List<CalibrationEvent> startCalibration() throws SeMoDeException {
        if (Files.exists(this.calibration.calibrationFile)) {
            log.info("Provider calibration already performed.");
            return List.of();
        }

        // execute linpack calibration
        return this.executeLinpackCalibration(this.platformPrefix);
    }

    private List<CalibrationEvent> executeLinpackCalibration(final String platformPrefix) throws SeMoDeException {
        AWSBenchmarkConfig config = this.calibrationConfig.getBenchmarkConfig();

        final List<CalibrationEvent> results = new ArrayList<>();
        for (int i = 0; i < this.calibrationConfig.getNumberOfAWSExecutions(); i++) {
            for (final int memory : config.getMemorySizeList()) {
                final String fileName = this.calibration.name + "/" + memory + "_" + i;
                final String pathForAPIGateway = platformPrefix + memory;
                if (this.client.doesS3ObjectExist(this.calibrationConfig.getBucketName(), "linpack/" + fileName)) {
                    log.info("Calibration already there - if you want to reexecute it, you have to remove it!");
                } else {
                    this.client.invokeLambdaFunction(config.getTargetUrl(), config.getApiKey(), pathForAPIGateway, fileName);
                    log.info("Invoke Lambda calibration function with " + memory + " MB");
                }
            }

            for (final int memory : config.getMemorySizeList()) {
                final String fileName = this.calibration.name + "/" + memory + "_" + i;
                this.client.waitForBucketObject(this.calibrationConfig.getBucketName(), "linpack/" + fileName, 600);
                final Path log = this.calibration.calibrationLogs.resolve(fileName);
                this.client.getFileFromBucket(this.calibrationConfig.getBucketName(), "linpack/" + fileName, log);
                results.add(new CalibrationEvent(i, memory, new LinpackParser(log).parseLinpack(), CalibrationPlatform.AWS));
            }
        }

        return results;
    }
}


