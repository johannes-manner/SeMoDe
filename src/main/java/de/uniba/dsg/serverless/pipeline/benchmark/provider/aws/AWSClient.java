package de.uniba.dsg.serverless.pipeline.benchmark.provider.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.util.concurrent.Uninterruptibles;
import de.uniba.dsg.serverless.pipeline.model.config.aws.AWSBenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AWSClient {

    private final AmazonS3 amazonS3Client;
    private final AmazonApiGateway amazonApiGatewayClient;
    private final AWSLambda amazonLambdaClient;
    private final AmazonIdentityManagement iamClient;

    /**
     * Creates an AWSClient
     *
     * @throws SeMoDeException if the AWS S3 Client could not be created (check credentials and
     *                         resources/awsEndpointInfo.json)
     */
    public AWSClient(final String region) throws SeMoDeException {
        try {
            this.amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(region).build();
            this.amazonApiGatewayClient = AmazonApiGatewayClientBuilder.standard().withRegion(region).build();
            this.amazonLambdaClient = AWSLambdaClientBuilder.standard().withRegion(region).build();
            this.iamClient = AmazonIdentityManagementClientBuilder.standard().withRegion(region).build();
        } catch (final AmazonServiceException e) {
            throw new SeMoDeException("AWS could not be accessed.", e);
        }
    }

    /**
     * Invokes an AWS Lambda function with respective memory. GET request, used for the calibration or for function
     * which specify a storage location to an S3 bucket, when data is generated.
     *
     * @param path schema is linpack_MEMORY
     */
    public void invokeLambdaFunction(final String targetUrl, final String apiKey, final String path, final String resultFileNameS3Bucket) throws SeMoDeException {
        ClientConfig configuration = new ClientConfig();
        // Avoid AWS Gateway Timeout by using local timeout of 1s
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 1000);
        final WebTarget lambdaTarget = ClientBuilder.newClient(configuration).target(targetUrl);
        try {
            final Response r = lambdaTarget.path(path)
                    .request()
                    .header("x-api-key", apiKey)
                    .post(Entity.entity("{\"resultFileName\": \"" + resultFileNameS3Bucket + "\"}", MediaType.APPLICATION_JSON));
            if (r.getStatus() == 403) {
                throw new SeMoDeException("Cannot invoke function. Forbidden (403).");
            }
        } catch (final ProcessingException ignored) {
            // expected, since the AWS gateway timeout < function execution
        }
    }

    /**
     * Blocking function that waits for a file, if not already available, to be created in the S3 bucket. If the timeout
     * is reached, an exception is thrown.
     *
     * @param keyName key of the file
     * @param timeout timeout in seconds
     * @throws SeMoDeException thrown if the S3 bucket is not available or the timeout is exceeded.
     */
    public void waitForBucketObject(final String bucketName, final String keyName, long timeout) throws SeMoDeException {
        log.info("Waiting for object " + keyName);
        timeout *= 1_000; // convert to ms
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + timeout) {
            if (this.amazonS3Client.doesObjectExist(bucketName, keyName)) {
                return;
            }
            Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
        }
        throw new SeMoDeException("Timeout exceeded, no object found.");
    }

    public boolean doesS3ObjectExist(String bucketName, String keyName) {
        return this.amazonS3Client.doesObjectExist(bucketName, keyName);
    }

    /**
     * Reads a file from S3 and stores it to the output path.
     *
     * @param keyName    path inside the S3 bucket
     * @param outputPath local path
     * @throws SeMoDeException If the file could not be accessed.
     */
    public void getFileFromBucket(final String bucketName, final String keyName, final Path outputPath) throws SeMoDeException {
        final S3Object s3Object = this.amazonS3Client.getObject(bucketName, keyName);
        try (final S3ObjectInputStream objectContent = s3Object.getObjectContent()) {
            Files.createDirectories(outputPath.getParent());
            try (final FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile())) {
                final byte[] readBuffer = new byte[1024];
                int length = 0;
                while ((length = objectContent.read(readBuffer)) > 0) {
                    fileOutputStream.write(readBuffer, 0, length);
                }
            }
        } catch (final IOException e) {
            throw new SeMoDeException("File could not be read.", e);
        }
    }

    public void deployLambdaFunction(final String functionName, final String runtime, final String awsArnRole, final String functionHandler, final int timeout, final int memorySize, final Path sourceZipPath) throws SeMoDeException {
        final CreateFunctionRequest createFunctionRequest;
        try {
            createFunctionRequest = new CreateFunctionRequest()
                    .withFunctionName(functionName)
                    .withRuntime(runtime)
                    .withRole(awsArnRole)
                    .withHandler(functionHandler)
                    .withCode(new FunctionCode()
                            .withZipFile(ByteBuffer.wrap(Files.readAllBytes(sourceZipPath))))
                    .withDescription("")
                    .withTimeout(timeout)
                    .withMemorySize(memorySize)
                    .withPublish(true);
        } catch (final IOException e) {
            throw new SeMoDeException("Can't read zip file " + sourceZipPath, e);
        }
        try {
            final CreateFunctionResult result = this.amazonLambdaClient.createFunction(createFunctionRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
                throw new SeMoDeException("Can't deploy lambda function. Inspect it: " + createFunctionRequest.toString());
            }
        } catch (final ResourceConflictException e) {
            log.warn("Lambda function already exists! Check if you need an update!");
        }
    }

    /**
     * Creates a AWS Gateway Rest API with an x-api-key for authorization.
     *
     * @param name of the api gateway
     * @return the id of the newly created API
     */
    public String createRestAPI(final String name) throws SeMoDeException {
        final CreateRestApiRequest createRestApiRequest = new CreateRestApiRequest()
                .withName(name)
                .withApiKeySource("HEADER")
                .withVersion("1.0");
        final CreateRestApiResult createRestAPIResult = this.amazonApiGatewayClient.createRestApi(createRestApiRequest);
        if (createRestAPIResult.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create Rest API " + name);
        }
        return createRestAPIResult.getId();
    }

    /**
     * Returns the parent resource ('/') of the ApiGateway RestAPI.
     */
    public String getParentResource(final String restApiId) throws SeMoDeException {
        final GetResourcesRequest getResourcesRequest = new GetResourcesRequest()
                .withRestApiId(restApiId);
        final GetResourcesResult result = this.amazonApiGatewayClient.getResources(getResourcesRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_OK) {
            throw new SeMoDeException("Can't get resources for rest api " + restApiId);
        }
        for (final Resource resource : result.getItems()) {
            // root resource has no parent
            if (resource.getParentId() == null) {
                return resource.getId();
            }
        }

        throw new SeMoDeException("There was an error by retrieving the root resource from the api " + restApiId);
    }

    /**
     * Creates the AWS Gateway Resource.
     */
    public String createRestResource(final String restApiId, final String parentResourceId, final String resourcePath) throws SeMoDeException {
        final CreateResourceRequest createResourceRequest = new CreateResourceRequest()
                .withRestApiId(restApiId)
                .withParentId(parentResourceId)
                .withPathPart(resourcePath);
        final CreateResourceResult result = this.amazonApiGatewayClient.createResource(createResourceRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create rest resource " + resourcePath + " for api " + restApiId + " and parent " + parentResourceId);
        }

        return result.getId();
    }

    /**
     * Creates the method and corresponding method response. ANY method is configured at this point for simplicity
     * reasons.
     */
    public void putMethodAndMethodResponse(final String restApiId, final String resourceId) throws SeMoDeException {
        this.putMethod(restApiId, resourceId);
        this.putMethodResponse(restApiId, resourceId);
    }

    private void putMethod(final String restApiId, final String resourceId) throws SeMoDeException {
        final PutMethodRequest putMethodRequest = new PutMethodRequest()
                .withRestApiId(restApiId)
                .withResourceId(resourceId)
                .withHttpMethod("ANY")
                .withAuthorizationType("NONE")
                .withApiKeyRequired(true);
        final PutMethodResult result = this.amazonApiGatewayClient.putMethod(putMethodRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create method for api " + restApiId + " and resource " + resourceId);
        }
    }

    private void putMethodResponse(final String restApiId, final String resourceId) throws SeMoDeException {
        final PutMethodResponseRequest putMethodResponseRequest = new PutMethodResponseRequest()
                .withRestApiId(restApiId)
                .withResourceId(resourceId)
                .withHttpMethod("ANY")
                .withStatusCode("200");
        final PutMethodResponseResult result = this.amazonApiGatewayClient.putMethodResponse(putMethodResponseRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create method response for api " + restApiId + " and resource " + resourceId);
        }
    }

    public void putIntegrationAndIntegrationResponse(final String restApiId, final String resourceId, final String functionName, final String region) throws SeMoDeException {
        this.putIntegration(restApiId, resourceId, functionName, region);
        this.putIntegrationResponse(restApiId, resourceId);
    }

    private void putIntegration(final String restApiId, final String resourceId, final String functionName, final String region) throws SeMoDeException {
        final PutIntegrationRequest putIntegrationRequest = new PutIntegrationRequest()
                .withRestApiId(restApiId)
                .withResourceId(resourceId)
                .withHttpMethod("ANY")
                .withType("AWS")
                .withIntegrationHttpMethod("POST")
                .withUri("arn:aws:apigateway:" + region + ":lambda:path/2015-03-31/functions/arn:aws:lambda:" + region + ":"
                        + this.extractAWSAccountId()
                        + ":function:" + functionName + "/invocations")
                .withPassthroughBehavior("WHEN_NO_MATCH");

        final PutIntegrationResult result = this.amazonApiGatewayClient.putIntegration(putIntegrationRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create integration for api " + restApiId + " and resource " + resourceId
                    + " with uri " + putIntegrationRequest.getUri());
        }
    }

    /**
     * The extraction here is somehow buddy, but if the user is not the root user, the user id of the subuser is not the
     * account id
     */
    private String extractAWSAccountId() {
        return this.iamClient.getUser().getUser().getArn().split(":")[4];
    }

    private void putIntegrationResponse(final String restApiId, final String resourceId) throws SeMoDeException {
        final PutIntegrationResponseRequest putIntegrationResponseRequest = new PutIntegrationResponseRequest()
                .withRestApiId(restApiId)
                .withResourceId(resourceId)
                .withHttpMethod("ANY")
                .withStatusCode("200")
                .withSelectionPattern(".*");

        final PutIntegrationResponseResult result = this.amazonApiGatewayClient.putIntegrationResponse(putIntegrationResponseRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create integration response for api " + restApiId + " and resource " + resourceId);
        }
    }

    public String createStage(final String restApiId, final String stageName) throws SeMoDeException {
        final CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest()
                .withRestApiId(restApiId)
                .withStageName(stageName);

        final CreateDeploymentResult result = this.amazonApiGatewayClient.createDeployment(createDeploymentRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create stage " + stageName + " for api " + restApiId);
        }

        return result.getId();
    }

    public String createUsagePlanAndUsagePlanKey(final String usagePlanName, final String restApiId, final String stageName, final String apiKeyId) throws SeMoDeException {
        final String usagePlanId = this.createUsagePlan(usagePlanName, restApiId, stageName);
        this.createUsagePlanKey(usagePlanId, apiKeyId);
        return usagePlanId;
    }

    public Pair<String, String> createApiKey(final String keyName, final String restApiId, final String stageName) throws SeMoDeException {
        final CreateApiKeyRequest createApiKeyRequest = new CreateApiKeyRequest()
                .withName(keyName)
                .withEnabled(true)
                .withStageKeys(new StageKey().withRestApiId(restApiId).withStageName(stageName));

        final CreateApiKeyResult result = this.amazonApiGatewayClient.createApiKey(createApiKeyRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create api key " + keyName + " for api " + restApiId + " and stage " + stageName);
        }

        return new ImmutablePair<String, String>(result.getId(), result.getValue());
    }

    private String createUsagePlan(final String usagePlanName, final String restApiId, final String stageName) throws SeMoDeException {
        final CreateUsagePlanRequest createUsagePlanRequest = new CreateUsagePlanRequest()
                .withName(usagePlanName)
                .withApiStages(new ApiStage().withApiId(restApiId).withStage(stageName));

        final CreateUsagePlanResult result = this.amazonApiGatewayClient.createUsagePlan(createUsagePlanRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't create usage plan " + usagePlanName + " for api " + restApiId + " and stage " + stageName);
        }

        return result.getId();
    }

    private void createUsagePlanKey(final String usagePlanId, final String apiKey) throws SeMoDeException {
        final CreateUsagePlanKeyRequest createUsagePlanKeyRequest = new CreateUsagePlanKeyRequest()
                .withUsagePlanId(usagePlanId)
                .withKeyType("API_KEY")
                .withKeyId(apiKey);

        final CreateUsagePlanKeyResult result = this.amazonApiGatewayClient.createUsagePlanKey(createUsagePlanKeyRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
            throw new SeMoDeException("Can't associate usage plan and key for usage plan " + usagePlanId + " and api key " + apiKey);
        }
    }

    public void updateLambdaPermission(final String functionName, final String restApiId, final String region) throws SeMoDeException {
        final AddPermissionRequest addPermissionRequest = new AddPermissionRequest()
                .withFunctionName(functionName)
                .withStatementId("default")
                .withAction("lambda:InvokeFunction")
                .withPrincipal("apigateway.amazonaws.com")
                .withSourceArn("arn:aws:execute-api:" + region + ":" + this.extractAWSAccountId()
                        + ":" + restApiId + "/*/*/" + functionName);

        try {
            final AddPermissionResult result = this.amazonLambdaClient.addPermission(addPermissionRequest);
            log.info("update lambda permission: " + result.getSdkHttpMetadata().getHttpStatusCode());
            if (result.getSdkHttpMetadata().getHttpStatusCode() != HttpStatus.SC_CREATED) {
                log.warn("Lambda function already exists! Permission update was not possible! Probably already there!");
            }
        } catch (final ResourceConflictException e) {
            log.warn("Lambda function permission already exists! Check if you need an update!");
        }
    }

    public void deleteLambdaFunction(final String functionName) {
        final DeleteFunctionRequest deleteFunctionRequest = new DeleteFunctionRequest()
                .withFunctionName(functionName);

        try {
            final DeleteFunctionResult result = this.amazonLambdaClient.deleteFunction(deleteFunctionRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_NO_CONTENT) {
                log.info("AWS Lambda function " + functionName + " deleted");
            }
        } catch (final ResourceNotFoundException e) {
            log.warn("AWS Lambda function " + functionName + " deleted or not present!");
        }
    }

    public void deleteApiKey(final String apiKeyId) {
        final DeleteApiKeyRequest deleteApiKeyRequest = new DeleteApiKeyRequest()
                .withApiKey(apiKeyId);

        try {
            final DeleteApiKeyResult result = this.amazonApiGatewayClient.deleteApiKey(deleteApiKeyRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_ACCEPTED) {
                log.info("Amazon Api Gateway key " + apiKeyId + " deleted");
            }
        } catch (final NotFoundException e) {
            log.warn("Amazon Api Gateway key " + apiKeyId + " deleted or not present!");
        }
    }

    public void deleteRestApi(final String restApiId) {
        final DeleteRestApiRequest deleteRestApiRequest = new DeleteRestApiRequest()
                .withRestApiId(restApiId);

        try {
            final DeleteRestApiResult result = this.amazonApiGatewayClient.deleteRestApi(deleteRestApiRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_ACCEPTED) {
                log.info("Amazon Api Gateway rest api " + restApiId + " deleted");
            }
        } catch (final NotFoundException e) {
            log.warn("Amazon Api Gateway rest api " + restApiId + "deleted or not present!");
        }
    }

    public void deleteUsagePlan(final String usagePlanId) {
        final DeleteUsagePlanRequest deleteUsagePlanRequest = new DeleteUsagePlanRequest()
                .withUsagePlanId(usagePlanId);

        try {
            final DeleteUsagePlanResult result = this.amazonApiGatewayClient.deleteUsagePlan(deleteUsagePlanRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_ACCEPTED) {
                log.info("Amazon Api Gateway rest usage plan " + usagePlanId + " deleted");
            }
        } catch (final NotFoundException e) {
            log.warn("Amazon Api Gateway rest usage plan " + usagePlanId + " deleted or not present!");
        }
    }

    public void deployLambdaFunctionAndHttpMethod(final String platformName, final Integer memorySize, AWSBenchmarkConfig benchmarkConfig) throws SeMoDeException {
        this.deployLambdaFunction(platformName, memorySize, benchmarkConfig);
        this.deployHttpMethodInRestApi(platformName, benchmarkConfig.getRestApiId(), benchmarkConfig.getRegion());
    }

    private void deployLambdaFunction(final String functionName, final int memorySize, final AWSBenchmarkConfig benchmarkConfig) throws SeMoDeException {
        // change directory to the linpack directory and zip it
        this.deployLambdaFunction(functionName, benchmarkConfig.getRuntime(), benchmarkConfig.getAwsArnLambdaRole(),
                benchmarkConfig.getFunctionHandler(), benchmarkConfig.getTimeout(), memorySize,
                Paths.get(benchmarkConfig.getPathToSource()));

        log.info("Function deployment successfully completed for " + memorySize + " MB! (AWS Lambda)");
    }

    /**
     * Deploys an API Gateway endpoint and secures this endpoint via a x-api-key. The x-api-key is also included in the
     * settings.json and generated by this deployment.
     */
    private void deployHttpMethodInRestApi(final String resourcePath, final String restApiId, final String region) throws SeMoDeException {
        final String parentResourceId = this.getParentResource(restApiId);
        final String resourceId = this.createRestResource(restApiId, parentResourceId, resourcePath);
        this.putMethodAndMethodResponse(restApiId, resourceId);
        this.putIntegrationAndIntegrationResponse(restApiId, resourceId, resourcePath, region);
    }

    public void enableRestApiUsage(final String restApiId, final String setupName, AWSBenchmarkConfig benchmarkConfig) throws SeMoDeException {
        final String stageName = setupName + "_stage";
        final String deploymentId = this.createStage(restApiId, stageName);
        final Pair<String, String> keyPair = this.createApiKey(setupName + "_key", restApiId, stageName);
        final String usagePlanId = this.createUsagePlanAndUsagePlanKey(setupName + "_plan", restApiId, stageName, keyPair.getKey());
        log.info("API Gateway deployment successfully completed!");

        // store x-api-key and targetUrl in pipeline configuration
        benchmarkConfig.setTargetUrl("https://" + restApiId + ".execute-api." + benchmarkConfig.getRegion() + ".amazonaws.com/" + setupName + "_stage");
        benchmarkConfig.setApiKeyId(keyPair.getKey());
        benchmarkConfig.setApiKey(keyPair.getValue());
        benchmarkConfig.setUsagePlanId(usagePlanId);
    }

    public void deleteApi(final AWSBenchmarkConfig deploymentInternals) {
        this.deleteApiKey(deploymentInternals.getApiKeyId());
        this.deleteRestApi(deploymentInternals.getRestApiId());
        this.deleteUsagePlan(deploymentInternals.getUsagePlanId());
    }

    public void deployFunctions(final String setupName, final List<Pair<String, Integer>> functionConfigs, AWSBenchmarkConfig benchmarkConfig) {
        try {
            // Create a single rest api for all function endpoints
            final String restApiId = this.createRestAPI(setupName);
            benchmarkConfig.setRestApiId(restApiId);

            for (final Pair<String, Integer> f : functionConfigs) {
                this.deployLambdaFunctionAndHttpMethod(f.getLeft(), f.getRight(), benchmarkConfig);
            }
            // set up rest api and stage
            this.enableRestApiUsage(restApiId, setupName, benchmarkConfig);

            // update lambda permission due to the enabled rest api
            for (final Pair<String, Integer> f : functionConfigs) {
                this.updateLambdaPermission(f.getLeft(), restApiId, benchmarkConfig.getRegion());
            }

            log.info("Deployment successfully completed!");
        } catch (final SeMoDeException e) {
            log.warn(e.getMessage());
            this.removeAllDeployedResources(functionConfigs, benchmarkConfig);
        }
    }

    /**
     * Removes all resources from aws cloud platform. <br/> Currently only the deployed lambda function and the api
     * gateway, key and usage plan
     */
    public void removeAllDeployedResources(final List<Pair<String, Integer>> functionConfigs, final AWSBenchmarkConfig benchmarkConfig) {

        for (final Pair<String, Integer> f : functionConfigs) {
            this.deleteLambdaFunction(f.getLeft());
        }

        this.deleteApi(benchmarkConfig);
    }
}
