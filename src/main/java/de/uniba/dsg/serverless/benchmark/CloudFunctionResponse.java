package de.uniba.dsg.serverless.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import de.uniba.dsg.serverless.util.FileLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a model class for the benchmark interaction to hold the
 * metainformation responded by the cloud function.
 * <p>
 * Metainformation include the platform ID, the container ID and the information
 * about the execution environment/VM configuration, if possible.
 */
public class CloudFunctionResponse {

    private static final ObjectReader jsonReader = new ObjectMapper().reader();
    private static final String CSV_SEPARATOR = System.getProperty("CSV_SEPARATOR");

    // mandatory elements to distinguish the different executions
    private static final String PLATFORM_ID = "platformId";
    private static final String CONTAINER_ID = "containerId";

    //optional VM information if available
    private static final String VM_ID = "vmIdentification";
    private static final String CPU_MODEL = "cpuModel";
    private static final String CPU_MODEL_NAME = "cpuModelName";
    private static final String DEFAULT_VALUE = "-";

    private final Map<String, String> metaInformation;
    private final String responseEntity;
    private final String uuid;

    private final FileLogger fileLogger;

    public CloudFunctionResponse(final String responseEntity, final String uuid, final FileLogger fileLogger) {

        this.responseEntity = responseEntity;
        this.uuid = uuid;
        this.fileLogger = fileLogger;

        this.metaInformation = new HashMap<>();
        for (final String key : CloudFunctionResponse.getMetdataKeys()) {
            this.metaInformation.put(key, DEFAULT_VALUE);
        }
    }

    public static List<String> getMetdataKeys() {
        final ArrayList<String> metadataKeys = new ArrayList<>();

        metadataKeys.add(PLATFORM_ID);
        metadataKeys.add(CONTAINER_ID);
        metadataKeys.add(VM_ID);
        metadataKeys.add(CPU_MODEL);
        metadataKeys.add(CPU_MODEL_NAME);

        return metadataKeys;
    }

    public void extractMetadata() {
        try {
            final JsonNode responseNode = jsonReader.readTree(this.responseEntity);

            for (final String key : this.metaInformation.keySet()) {
                if (responseNode.has(key)) {
                    this.metaInformation.put(key, responseNode.get(key).asText());
                }
            }
        } catch (final IOException e) {
            // swallow the exception, because meta information is optional
            // only relevant when linking the local and remote execution on the platform
            this.fileLogger.warning("Error parsing the response from the server. The response should be a json.");
        }
    }

    public void logMetadata(final String platform) {

        for (final String key : this.metaInformation.keySet()) {
            this.fileLogger.info(CSV_SEPARATOR + platform + CSV_SEPARATOR + key.toUpperCase() + CSV_SEPARATOR + this.uuid + CSV_SEPARATOR + this.metaInformation.get(key));
        }
    }
}
