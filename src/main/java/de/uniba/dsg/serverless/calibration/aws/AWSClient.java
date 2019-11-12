package de.uniba.dsg.serverless.calibration.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class AWSClient {

    private static final Logger logger = LogManager.getLogger(AWSClient.class.getName());

    private final AmazonS3 amazonS3Client;
    private final WebTarget lambdaTarget;

    private final String bucketName;
    private final String apiKey;

    /**
     * Creates an AWSClient
     *
     * @param targetUrl  target URL of linpack calibration
     * @param apiKey     AWS api key
     * @param bucketName bucket name where results are stored
     * @throws SeMoDeException if the AWS S3 Client could not be created (check credentials and resources/awsEndpointInfo.json)
     */
    public AWSClient(String targetUrl, String apiKey, String bucketName) throws SeMoDeException {
        try {
            amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
        } catch (AmazonServiceException e) {
            throw new SeMoDeException("AWS could not be accessed.", e);
        }
        ClientConfig configuration = new ClientConfig();
        // Avoid AWS Gateway Timeout by using local timeout of 1s
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 1000);
        lambdaTarget = ClientBuilder.newClient(configuration).target(targetUrl);
        this.bucketName = bucketName;
        this.apiKey = apiKey;
    }

    /**
     * Invokes the Benchmark function with respective memory and returns the response.
     *
     * @param memory memory setting (specified in linpack/aws)
     */
    public void invokeBenchmarkFunctions(int memory, String resultFileName) throws SeMoDeException {
        String path = "linpack_" + memory;
        try {
            Response r = lambdaTarget.path(path)
                    .queryParam("resultFileName", resultFileName)
                    .request()
                    .header("x-api-key", apiKey)
                    .get();
            if (r.getStatus() == 403) {
                throw new SeMoDeException("Cannot invoke function. Forbidden (403).");
            }
        } catch (ProcessingException ignored) {
            // expected, since the AWS gateway timeout < function execution
        }
    }

    /**
     * Blocking function that waits for a file to be created in the S3 bucket. If the timeout is reached, an exception is thrown
     *
     * @param keyName key of the file
     * @param timeout timeout in seconds
     * @throws SeMoDeException thrown if the S3 bucket is not available or the timeout is exceeded.
     */
    public void waitForBucketObject(String keyName, long timeout) throws SeMoDeException {
        logger.info("Waiting for object " + keyName);
        timeout *= 1_000; // convert to ms
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + timeout) {
            if (amazonS3Client.doesObjectExist(bucketName, keyName)) {
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
    public void getFileFromBucket(String keyName, Path outputPath) throws SeMoDeException {
        S3Object s3Object = amazonS3Client.getObject(bucketName, keyName);
        try (S3ObjectInputStream objectContent = s3Object.getObjectContent()) {
            Files.createDirectories(outputPath.getParent());
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile())) {
                byte[] readBuffer = new byte[1024];
                int length = 0;
                while ((length = objectContent.read(readBuffer)) > 0) {
                    fileOutputStream.write(readBuffer, 0, length);
                }
            }
        } catch (IOException e) {
            throw new SeMoDeException("File could not be read.", e);
        }
    }


}
