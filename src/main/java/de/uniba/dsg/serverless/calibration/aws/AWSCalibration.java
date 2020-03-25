package de.uniba.dsg.serverless.calibration.aws;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationMethods;
import de.uniba.dsg.serverless.calibration.LinpackParser;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSFunctionConfig;
import de.uniba.dsg.serverless.util.FileLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AWSCalibration implements CalibrationMethods {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private final AWSCalibrationConfig config;
    private final AWSFunctionConfig functionConfig;
    private final AWSClient client;
    // composite
    private final Calibration calibration;

    // prefix to identify the function / functions and the api gateway url
    // suffix is the memory setting
    private final String platformPrefix;

    // used for CLI feature
    public AWSCalibration(final String name, final AWSCalibrationConfig config) throws SeMoDeException {
        this.calibration = new Calibration(name + "_calibration", SupportedPlatform.AWS);
        this.config = config;
        this.functionConfig = config.functionConfig;
        this.client = new AWSClient(this.functionConfig.region);
        this.platformPrefix = this.calibration.name + "_linpack_";
    }

    // used within pipeline
    public AWSCalibration(final String name, final Path calibrationFolder, final AWSCalibrationConfig config) throws SeMoDeException {
        this.calibration = new Calibration(name + "_calibration", SupportedPlatform.AWS, calibrationFolder);
        this.config = config;
        this.functionConfig = config.functionConfig;
        this.client = new AWSClient(this.functionConfig.region);
        this.platformPrefix = this.calibration.name + "_linpack_";
    }

    private List<Pair<String, Integer>> generateFunctionNames() {
        // create function names
        final List<Pair<String, Integer>> functionConfigs = new ArrayList<>();
        for (final Integer memorySize : this.functionConfig.memorySizes) {
            functionConfigs.add(new ImmutablePair<>(this.platformPrefix + memorySize, memorySize));
        }
        return functionConfigs;
    }

    /**
     * Prior to the calibration, a decision is made by the user, if the linpack deployment should be executed.
     * See also {@link AWSClient#removeAllDeployedResources}, if a error occurred during deployment and remove all deployed resources from the platform.
     *
     * @throws SeMoDeException
     */
    @Override
    public void performCalibration() throws SeMoDeException {
        if (Files.exists(this.calibration.calibrationFile)) {
            System.out.println("Provider calibration already performed.");
            return;
        }

        // deploy linpack if user specified it
        if (this.config.deployLinpack) {
            final List<Pair<String, Integer>> functionConfigs = this.generateFunctionNames();
            this.client.deployFunctions(this.calibration.name, functionConfigs, this.functionConfig, this.config.deploymentInternals);
        }

        // execute linpack calibration
        this.executeLinpackCalibration(this.platformPrefix);

    }

    @Override
    public void stopCalibration() {
        final List<Pair<String, Integer>> functionConfigs = this.generateFunctionNames();
        this.client.removeAllDeployedResources(functionConfigs, this.config.deploymentInternals);
        this.config.resetConfig();
    }

    private void executeLinpackCalibration(final String platformPrefix) throws SeMoDeException {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.functionConfig.memorySizes.stream().map(String::valueOf).collect(Collectors.joining(",")));
        sb.append("\n");

        for (int i = 0; i < this.config.numberOfAWSExecutions; i++) {
            for (final int memory : this.functionConfig.memorySizes) {
                final String fileName = this.calibration.name + "/" + memory + "_" + i;
                final String pathForAPIGateway = platformPrefix + memory;
                this.client.invokeLambdaFunction(this.functionConfig.targetUrl, this.functionConfig.apiKey, pathForAPIGateway, fileName);
            }
            final List<Double> results = new ArrayList<>();
            for (final int memory : this.functionConfig.memorySizes) {
                final String fileName = this.calibration.name + "/" + memory + "_" + i;
                this.client.waitForBucketObject(this.config.bucketName, "linpack/" + fileName, 600);
                final Path log = this.calibration.calibrationLogs.resolve(fileName);
                this.client.getFileFromBucket(this.config.bucketName, "linpack/" + fileName, log);
                results.add(new LinpackParser(log).parseLinpack());
            }
            sb.append(results.stream().map(this.calibration.DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
            sb.append("\n");
        }
        try {
            Files.write(this.calibration.calibrationFile, sb.toString().getBytes());
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
    }
}


