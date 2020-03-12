package de.uniba.dsg.serverless.calibration.aws;

import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationMethods;
import de.uniba.dsg.serverless.calibration.LinpackParser;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSFunctionConfig;
import de.uniba.dsg.serverless.util.SeMoDeProperty;
import de.uniba.dsg.serverless.util.SeMoDePropertyManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AWSCalibration implements CalibrationMethods {

    private static final Logger logger = LogManager.getLogger(AWSCalibration.class.getName());
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
        this.calibration = new Calibration(name, SupportedPlatform.AWS);
        this.config = config;
        this.functionConfig = config.functionConfig;
        this.client = new AWSClient(this.functionConfig.region);
        this.platformPrefix = this.calibration.name + "_linpack_";
    }

    // used within pipeline
    public AWSCalibration(final String name, final Path calibrationFolder, final AWSCalibrationConfig config) throws SeMoDeException {
        this.calibration = new Calibration(name, SupportedPlatform.AWS, calibrationFolder);
        this.config = config;
        this.functionConfig = config.functionConfig;
        this.client = new AWSClient(this.functionConfig.region);
        this.platformPrefix = this.calibration.name + "_linpack_";
    }

    /**
     * Prior to the calibration, a decision is made by the user, if the linpack deployment should be executed.
     * See also {@link #removeAllDeployedResources()}, if a error occurred during deployment and remove all deployed resources from the platform.
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
        try {
            if (this.config.deployLinpack) {
                // Create a single rest api for all function endpoints
                final String restApiId = this.client.createRestAPI(this.calibration.name);
                this.config.deploymentInternals.restApiId = restApiId;

                for (final Integer memorySize : this.functionConfig.memorySizes) {
                    // platform name is used for the name of the aws lambda function
                    // and the path attribute for the aws api gateway
                    final String platformName = this.platformPrefix + memorySize;
                    this.deployLambdaFunction(platformName, memorySize);
                    this.deployHttpMethodInRestApi(platformName, restApiId);
                }
                // set up rest api and stage
                this.enableRestApiUsage(restApiId);

                // update lambda permission due to the enabled rest api
                for (final Integer memorySize : this.functionConfig.memorySizes) {
                    this.updateLambdaPermission(this.platformPrefix + memorySize, restApiId, this.functionConfig.region);
                }

                System.out.println("Deployment successfully completed!");
            }
        } catch (final SeMoDeException e) {
            //Error during deployment - remove all deployed resources
            this.removeAllDeployedResources();
            throw e;
        }

        // execute linpack calibration
        this.executeLinpackCalibration(this.platformPrefix);

    }

    @Override
    public void stopCalibration() {
        this.removeAllDeployedResources();
    }

    /**
     * Removes all resources from aws cloud platform. </br>
     * Currently only the deployed lambda function and the api gateway, key and usage plan
     */
    public void removeAllDeployedResources() {

        for (final Integer memorySize : this.functionConfig.memorySizes) {
            this.client.deleteLambdaFunction(this.platformPrefix + memorySize);
        }

        this.client.deleteApiKey(this.config.deploymentInternals.apiKeyId);
        this.client.deleteRestApi(this.config.deploymentInternals.restApiId);
        this.client.deleteUsagePlan(this.config.deploymentInternals.usagePlanId);
    }

    private void enableRestApiUsage(final String restApiId) throws SeMoDeException {
        final String stageName = this.calibration.name + "_stage";
        final String deploymentId = this.client.createStage(restApiId, stageName);
        final Pair<String, String> keyPair = this.client.createApiKey(this.calibration.name + "_key", restApiId, stageName);
        final String usagePlanId = this.client.createUsagePlanAndUsagePlanKey(this.calibration.name + "_plan", restApiId, stageName, keyPair.getKey());
        System.out.println("API Gateway deployment successfully completed!");

        // store x-api-key and targetUrl in pipeline configuration
        this.functionConfig.targetUrl = "https://" + restApiId + ".execute-api." + this.functionConfig.region + ".amazonaws.com/" + this.calibration.name + "_stage";
        this.config.deploymentInternals.apiKeyId = keyPair.getKey();
        this.functionConfig.apiKey = keyPair.getValue();
        this.config.deploymentInternals.usagePlanId = usagePlanId;
    }

    /**
     * Deploys an API Gateway endpoint and secures this endpoint via a x-api-key.
     * The x-api-key is also included in the settings.json and generated by this deployment.
     */
    private void deployHttpMethodInRestApi(final String resourcePath, final String restApiId) throws SeMoDeException {
        final String parentResourceId = this.client.getParentResource(restApiId);
        final String resourceId = this.client.createRestResource(restApiId, parentResourceId, resourcePath);
        this.client.putMethodAndMethodResponse(restApiId, resourceId);
        this.client.putIntegrationAndIntegrationResponse(restApiId, resourceId, resourcePath, this.functionConfig.region);
    }

    private void deployLambdaFunction(final String functionName, final int memorySize) throws SeMoDeException {
        // change directory to the linpack directory and zip it
        this.executeBashCommand("cd " + this.functionConfig.pathToSource + "; zip function.zip *");
        this.client.deployLambdaFunction(functionName, this.functionConfig.runtime, this.functionConfig.awsArnLambdaRole,
                this.functionConfig.functionHandler, this.functionConfig.timeout, memorySize,
                Paths.get(this.functionConfig.pathToSource + "/function.zip"));

        System.out.println("Linpack deployment successfully completed for " + memorySize + " MB! (AWS Lambda)");
    }

    private void updateLambdaPermission(final String platformName, final String restApiId, final String region) throws SeMoDeException {
        this.client.updateLambdaPermission(platformName, restApiId, region);
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

    private void executeBashCommand(final String command) throws SeMoDeException {
        final ProcessBuilder processBuilder = new ProcessBuilder(SeMoDePropertyManager.getInstance().getProperty(SeMoDeProperty.BASH_LOCATION), "-c", command);
        Process process = null;
        try {
            process = processBuilder.start();
            final int errCode = process.waitFor();
            System.out.println("Executed without errors? " + (errCode == 0 ? "Yes" : "No(code=" + errCode + ")"));
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
            process.destroy();
        }
    }
}


