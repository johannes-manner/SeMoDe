package de.uniba.dsg.serverless.pipeline.benchmark.model;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uniba.dsg.serverless.pipeline.benchmark.util.LoadPatternGenerator;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientProperties;

@Slf4j
public class FunctionTrigger implements Callable<LocalRESTEvent> {

    private static final int REQUEST_PASSED_STATUS = 200;

    private final String platform;
    private final String host;
    private final String path;
    private final String jsonInput;
    private final Map<String, String> queryParameters;
    private final Map<String, String> headerParameters;

    public FunctionTrigger(final String platform, final String jsonInput, final URL url, final Map<String, String> headerValues) {

        this.platform = platform;
        this.jsonInput = jsonInput;
        this.headerParameters = headerValues;

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
    public LocalRESTEvent call() throws SeMoDeException {

        LocalRESTEvent localRESTEvent = new LocalRESTEvent();
        final String uuid = UUID.randomUUID().toString();

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

            log.info(uuid + "\tStart request on '" + this.platform + "'");
            localRESTEvent.setStartTime(LocalDateTime.now());

            response = invocation.post(Entity.entity(this.jsonInput, MediaType.APPLICATION_JSON));

            log.info(uuid + "\tEnd request on '" + this.platform + "'");
            localRESTEvent.setEndTime(LocalDateTime.now());
        } catch (final RuntimeException e) {
            log.warn(uuid + "\tRequest on '" + this.platform + "' returned an error");
            localRESTEvent.setEndTime(LocalDateTime.now());

            throw new SeMoDeException("Can't submit request", e);
        }

        if (response.getStatus() != REQUEST_PASSED_STATUS) {
            log.warn(uuid + "\tRequest on '" + this.platform + "' returned an error" + response.getStatus() + " - " + response.getStatusInfo());
            throw new SeMoDeException("Request exited with an error: " + response.getStatus() + " - " + response.getStatusInfo());
        }

        try {
            ProviderEvent responseEntity = response.readEntity(ProviderEvent.class);
            localRESTEvent.setProviderEvent(responseEntity);
        } catch (Exception e) {
            log.warn("Terminate benchmark execution since the response does not map the ProviderEvent model class. Check your cloud function response!");
            throw new SeMoDeException("Cloud function response does not map the ProviderEvent model of our application", e);
        }

        return localRESTEvent;
    }
}