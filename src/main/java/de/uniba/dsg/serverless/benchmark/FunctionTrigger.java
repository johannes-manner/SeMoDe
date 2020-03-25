package de.uniba.dsg.serverless.benchmark;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.util.FileLogger;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class FunctionTrigger implements Callable<String> {


    private static final String CSV_SEPARATOR = System.getProperty("CSV_SEPARATOR");

    private static final int REQUEST_PASSED_STATUS = 200;

    private final String platform;
    private final String host;
    private final String path;
    private final String jsonInput;
    private final Map<String, String> queryParameters;
    private final Map<String, String> headerParameters;

    private final FileLogger logger;

    public FunctionTrigger(final String platform, final String jsonInput, final URL url, final Map<String, String> headerValues, final FileLogger fileLogger) {

        this.platform = platform;
        this.jsonInput = jsonInput;
        this.headerParameters = headerValues;
        this.logger = fileLogger;

        String tempHost = url.getProtocol() + "://" + url.getHost();
        if (url.getPort() != -1) {
            tempHost = tempHost + ":" + url.getPort();
        }

        this.host = tempHost;
        this.path = url.getPath();

        this.queryParameters = new HashMap<>();
        final String queryString = url.getQuery();
        if (queryString != null) {
            final String[] queries = url.getQuery().split("&");
            for (final String query : queries) {
                final int pos = query.indexOf('=');
                this.queryParameters.put(query.substring(0, pos), query.substring(pos + 1));
            }
        }
    }

    @Override
    public String call() throws SeMoDeException {

        final String uuid = UUID.randomUUID().toString();
        this.logger.info(this.platform + CSV_SEPARATOR + "START" + CSV_SEPARATOR + uuid);

        final Client client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, LoadPatternGenerator.PLATFORM_FUNCTION_TIMEOUT * 1000);
        client.property(ClientProperties.READ_TIMEOUT, LoadPatternGenerator.PLATFORM_FUNCTION_TIMEOUT * 1000);

        WebTarget target = client.target(this.host).path(this.path);

        for (final String key : this.queryParameters.keySet()) {
            target = target.queryParam(key, this.queryParameters.get(key));
        }

        Response response = null;
        try {
            Invocation.Builder invocation = target.request(MediaType.APPLICATION_JSON_TYPE);
            // add header
            for (final String key : this.headerParameters.keySet()) {
                invocation = invocation.header(key, this.headerParameters.get(key));
            }
            response = invocation.post(Entity.entity(this.jsonInput, MediaType.APPLICATION_JSON));
        } catch (final RuntimeException e) {
            this.logger.info(this.platform + CSV_SEPARATOR + "END" + CSV_SEPARATOR + uuid);
            this.logger.warning(this.platform + CSV_SEPARATOR + "ERROR" + CSV_SEPARATOR + uuid);
            throw new SeMoDeException("Can't submit request", e);
        }
        this.logger.info(this.platform + CSV_SEPARATOR + "END" + CSV_SEPARATOR + uuid);


        if (response.getStatus() != REQUEST_PASSED_STATUS) {
            this.logger.warning(this.platform + CSV_SEPARATOR + "ERROR" + CSV_SEPARATOR + uuid + CSV_SEPARATOR + response.getStatus() + " - "
                    + response.getStatusInfo());
            throw new SeMoDeException(
                    "Request exited with an error: " + response.getStatus() + " - " + response.getStatusInfo());
        }

        // the response entity has to be a json representation with a platformId,
        // result key and a containerId
        final String responseEntity = response.readEntity(String.class);

        final CloudFunctionResponse functionResponse = new CloudFunctionResponse(responseEntity, uuid, this.logger);
        functionResponse.extractMetadata();
        functionResponse.logMetadata(this.platform);

        final String responseValue = response.getStatus() + " " + responseEntity;

        return responseValue;
    }

}