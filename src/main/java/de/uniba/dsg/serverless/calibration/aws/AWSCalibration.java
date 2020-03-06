package de.uniba.dsg.serverless.calibration.aws;

import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.calibration.LinpackParser;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.util.SeMoDeProperty;
import de.uniba.dsg.serverless.util.SeMoDePropertyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AWSCalibration extends Calibration {

    private static final Logger logger = LogManager.getLogger(AWSCalibration.class.getName());
    private final AWSClient client;
    private final AWSCalibrationConfig config;

    // used for CLI feature
    public AWSCalibration(final String name, final AWSCalibrationConfig config) throws SeMoDeException {
        super(name, CalibrationPlatform.AWS);
        this.client = new AWSClient(config.region);
        this.config = config;
    }

    // used within pipeline
    public AWSCalibration(final String name, final Path calibrationFolder, final AWSCalibrationConfig config) throws SeMoDeException {
        super(name, CalibrationPlatform.AWS, calibrationFolder);
        this.client = new AWSClient(config.region);
        this.config = config;
    }

    public void performCalibration() throws SeMoDeException {
        if (Files.exists(this.calibrationFile)) {
            System.out.println("Provider calibration already performed.");
            return;
        }

        // deploy linpack if user specified it
        if (this.config.deployLinpack) {
            // TODO make this configurable
            // platform name is used for the name of the aws lambda function
            // and the path attribute for the aws api gateway
            final String platformName = "linpack_test_128";
            this.deployLinpack(platformName, 128);
            this.deployAPIGateway(platformName);
        }

        // execute linpack calibration
        this.executeLinpackCalibration();

    }

    /**
     * Deploys an API Gateway endpoint and secures this endpoint via a x-api-key.
     * The x-api-key is also included in the settings.json and generated by this deployment.
     */
    private void deployAPIGateway(final String resourcePath) throws SeMoDeException {
        final String restApiId = this.client.createRestAPI(this.name);
        final String parentResourceId = this.client.getParentResource(restApiId);
        final String resourceId = this.client.createRestResource(restApiId, parentResourceId, resourcePath);
        this.client.putMethodAndMethodResponse(restApiId, resourceId);
        this.client.putIntegrationAndIntegrationResponse(restApiId, resourceId, resourcePath, this.config.region);

        System.out.println("API Gateway deployment successfully completed!");
    }

    private void deployLinpack(final String functionName, final int memorySize) throws SeMoDeException {
        this.executeBashCommand("zip linpack/aws/linpack.zip linpack/aws/*");
        this.client.deployLambdaFunction(functionName, this.config.runtime, this.config.awsArnLambdaRole, this.config.functionHandler, this.config.timeout, memorySize, Paths.get("linpack/aws/linpack.zip"));

        System.out.println("Linpack deployment successfully completed! (AWS Lambda)");
    }

    private void executeLinpackCalibration() throws SeMoDeException {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.config.memorySizes.stream().map(String::valueOf).collect(Collectors.joining(",")));
        sb.append("\n");

        for (int i = 0; i < this.config.numberOfAWSExecutions; i++) {
            for (final int memory : this.config.memorySizes) {
                final String fileName = this.name + "/" + memory + "_" + i;
                final String pathForAPIGateway = "linpack_" + memory;
                this.client.invokeLambdaFunction(this.config.targetUrl, this.config.apiKey, pathForAPIGateway, fileName);
            }
            final List<Double> results = new ArrayList<>();
            for (final int memory : this.config.memorySizes) {
                final String fileName = this.name + "/" + memory + "_" + i;
                this.client.waitForBucketObject(this.config.bucketName, "linpack/" + fileName, 600);
                final Path log = this.calibrationLogs.resolve(fileName);
                this.client.getFileFromBucket(this.config.bucketName, "linpack/" + fileName, log);
                results.add(new LinpackParser(log).parseLinpack());
            }
            sb.append(results.stream().map(this.DOUBLE_FORMAT::format).collect(Collectors.joining(",")));
            sb.append("\n");
        }
        try {
            Files.write(this.calibrationFile, sb.toString().getBytes());
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


