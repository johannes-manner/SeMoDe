package de.uniba.dsg.serverless.pipeline.benchmark.log.ibm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import de.uniba.dsg.serverless.pipeline.benchmark.log.LogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.util.SeMoDeException;

/**
 * There is no Java SDK for OpenWhisk and there is also no other support. But there is a REST Api which support
 * fundamental management utilities.
 * <p/>
 * Configuration: install cli, add the plugin cloud-functions, get the authorization value by calling a function's REST
 * interface in the verbose mode.
 *
 * @see <a href= "http://petstore.swagger.io/?url=https://raw.githubusercontent.com/openwhisk/openwhisk/master/core/controller/src/main/resources/apiv1swagger.json#/">
 * REST Api of OpenWhisk</a>
 */
@Deprecated(since = "January 2021")
public class IBMLogHandler implements LogHandler {

    private final String namespace;
    private final String functionName;
    private final String authorizationToken;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public IBMLogHandler(final String namespace, final String functionName, final String authorizationToken, final LocalDateTime startTime,
                         final LocalDateTime endTime) {
        this.namespace = namespace;
        this.functionName = functionName;
        this.authorizationToken = authorizationToken;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public List<PerformanceData> getPerformanceData() throws SeMoDeException {
//        final List<JsonNode> activationsArray = this.getActivations();
//        final List<PerformanceData> performanceDataList = this.extractPerformanceData(activationsArray);
//
//        final Map<String, WritableEvent> eventMap = new HashMap<>();
//        for (final PerformanceData data : performanceDataList) {
//            eventMap.put(data.getPlatformId(), data);
//        }
//        return eventMap;
        return List.of();
    }

    private List<PerformanceData> extractPerformanceData(final List<JsonNode> activationsArray) {

        final List<PerformanceData> performanceData = new ArrayList<>();

        for (final JsonNode activation : activationsArray) {
            final JsonNode result = activation.get("response").get("result");
            double startUpDuration = -1.0;
            int memoryUsed = -1;
            for (final JsonNode n : activation.get("annotations")) {
                final String key = n.get("key").asText();
                if ("initTime".equals(key)) {
                    startUpDuration = n.get("value").asDouble();
                } else if ("limits".equals(key)) {
                    memoryUsed = n.get("value").get("memory").asInt();
                }
            }
            final PerformanceData data = new PerformanceData(this.functionName, result.get("containerId").asText(),
                    result.get("platformId").asText(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(activation.get("start").asLong()),
                            ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(activation.get("end").asLong()),
                            ZoneId.systemDefault()),
                    startUpDuration, activation.get("duration").asDouble(), activation.get("duration").asInt(),
                    memoryUsed, memoryUsed);

            performanceData.add(data);
        }
        return performanceData;
    }

    /**
     * OpenWhisk offers no sdk for java developers and also other sdk implementations are in its infancy. The method
     * here returns all activations and uses a default limit of 200 activations. Are there more than 200 activations in
     * the specified time span, additional REST calls are made to get all these activations.
     * <p/>
     *
     * @see <a href= "http://petstore.swagger.io/?url=https://raw.githubusercontent.com/openwhisk/openwhisk/master/core/controller/src/main/resources/apiv1swagger.json#/Activations">Activations
     * in API</a>
     */
    private List<JsonNode> getActivations() throws SeMoDeException {

        // maximum limit setting by June 2018
        final int limit = 200;
        long endMillis = this.endTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
        boolean furtherElements = true;
        final List<JsonNode> activationList = new ArrayList<>();

        do {
            final Client client = ClientBuilder.newClient();
            final Response response = client.target("https://openwhisk.eu-gb.bluemix.net")
                                            .path("/api/v1/namespaces/" + this.namespace + "/activations")
                                            .queryParam("limit", limit)
                                            .queryParam("name", this.functionName)
                                            .queryParam("since", this.startTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli())
                                            .queryParam("upto", endMillis)
                                            .queryParam("docs", "true")
                                            .request(MediaType.APPLICATION_JSON_TYPE)
                                            .header("Authorization", this.authorizationToken).get();

            if (response.getStatus() == 200) {
                final JsonNode arrayNode = response.readEntity(JsonNode.class);
                JsonNode activation = null;
                for (int i = 0; i < arrayNode.size(); i++) {
                    activation = arrayNode.get(i);
                    activationList.add(activation);
                }
                // Another request is needed to get all activation from startTime until endTime
                // openWhisk returns always the most recent events in descending order
                // therefore the end time must be reassigned to the start time of the last element
                // in the array node
                if (arrayNode.size() == limit) {
                    endMillis = activation.get("start").asLong() - 1;
                } else {
                    furtherElements = false;
                }
            } else {
                throw new SeMoDeException("Error by retrieving the activations from OpenWhisk REST interface.");
            }
        } while (furtherElements);

        return activationList;
    }
}
