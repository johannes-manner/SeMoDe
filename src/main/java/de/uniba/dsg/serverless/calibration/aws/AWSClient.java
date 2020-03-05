package de.uniba.dsg.serverless.calibration.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.util.concurrent.Uninterruptibles;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class AWSClient {

    private static final Logger logger = LogManager.getLogger(AWSClient.class.getName());

    private final AmazonS3 amazonS3Client;
    private final AmazonApiGateway amazonApiGatewayClient;
    private final AWSLambda amazonLambdaClient;

    /**
     * Creates an AWSClient
     *
     * @throws SeMoDeException if the AWS S3 Client could not be created (check credentials and resources/awsEndpointInfo.json)
     */
    public AWSClient(final String region) throws SeMoDeException {
        try {
            this.amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(region).build();
            this.amazonApiGatewayClient = AmazonApiGatewayClientBuilder.standard().withRegion(region).build();
            this.amazonLambdaClient = AWSLambdaClientBuilder.standard().withRegion(region).build();
        } catch (final AmazonServiceException e) {
            throw new SeMoDeException("AWS could not be accessed.", e);
        }
    }

    /**
     * Invokes an AWS Lambda function with respective memory and returns the response.
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
                    .queryParam("resultFileName", resultFileNameS3Bucket)
                    .request()
                    .header("x-api-key", apiKey)
                    .get();
            if (r.getStatus() == 403) {
                throw new SeMoDeException("Cannot invoke function. Forbidden (403).");
            }
        } catch (final ProcessingException ignored) {
            // expected, since the AWS gateway timeout < function execution
        }
    }

    /**
     * Blocking function that waits for a file, if not already available, to be created in the S3 bucket.
     * If the timeout is reached, an exception is thrown.
     *
     * @param keyName key of the file
     * @param timeout timeout in seconds
     * @throws SeMoDeException thrown if the S3 bucket is not available or the timeout is exceeded.
     */
    public void waitForBucketObject(final String bucketName, final String keyName, long timeout) throws SeMoDeException {
        logger.info("Waiting for object " + keyName);
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
        final CreateFunctionResult result = this.amazonLambdaClient.createFunction(createFunctionRequest);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != 201) {
            throw new SeMoDeException("Can't deploy lambda function. Inspect it: " + createFunctionRequest.toString());
        }
    }
}
