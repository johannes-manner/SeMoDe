package de.uniba.dsg.serverless.benchmark;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class FunctionTrigger implements Callable<String> {

	// only use the logger in this class for logging the REST data - see
	// log4j2-test.xml
	private static final Logger logger = LogManager.getLogger(FunctionTrigger.class.getName());

	private static final String CSV_SEPARATOR = System.getProperty("CSV_SEPARATOR");

	// used in the response header element to map the executions on the cloud
	// platform and containerId
	private static final String PLATFORM_ID = "platformId";
	private static final String CONTAINER_ID = "containerId";
	
	private static final ObjectReader jsonReader = new ObjectMapper().reader();

	private static final int REQUEST_PASSED_STATUS = 200;

	private final String host;
	private final String path;
	private final Map<String, String> queryParameters;
	private final String jsonInput;

	public FunctionTrigger(String jsonInput, URL url) {

		this.jsonInput = jsonInput;

		String tempHost = url.getProtocol() + "://" + url.getHost();
		if( url.getPort() != -1) {
			tempHost = tempHost +  ":" + url.getPort();
		}
		
		this.host = tempHost;
		this.path = url.getPath();

		this.queryParameters = new HashMap<>();
		String queryString = url.getQuery();
		if (queryString != null) {
			String[] queries = url.getQuery().split("&");
			for (String query : queries) {
				int pos = query.indexOf('=');
				this.queryParameters.put(query.substring(0, pos), query.substring(pos + 1));
			}
		}
	}

	@Override
	public String call() throws SeMoDeException {

		String uuid = UUID.randomUUID().toString();
		logger.info("START" + CSV_SEPARATOR + uuid);

		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, LoadPatternGenerator.PLATFORM_FUNCTION_TIMEOUT * 1000);
		client.property(ClientProperties.READ_TIMEOUT, LoadPatternGenerator.PLATFORM_FUNCTION_TIMEOUT * 1000);

		WebTarget target = client.target(host).path(path);

		for (String key : queryParameters.keySet()) {
			target = target.queryParam(key, queryParameters.get(key));
		}

		Response response = null;
		try {
			response = target.request(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.entity(jsonInput, MediaType.APPLICATION_JSON));
		} catch (RuntimeException e) {
			logger.info("END" + CSV_SEPARATOR + uuid);
			logger.warn("ERROR" + CSV_SEPARATOR + uuid);
			throw e;
		}
		logger.info("END" + CSV_SEPARATOR + uuid);


		if (response.getStatus() != REQUEST_PASSED_STATUS) {
			logger.warn("ERROR" + CSV_SEPARATOR + uuid + CSV_SEPARATOR + response.getStatus() + " - "
					+ response.getStatusInfo());
			throw new SeMoDeException(
					"Request exited with an error: " + response.getStatus() + " - " + response.getStatusInfo());
		}

		// the response entity has to be a json representation with a platformId,
		// result key and a containerId
		String responseEntity = response.readEntity(String.class);
		String platformId = "";
		String containerId = "";
		try {
			JsonNode responseNode = jsonReader.readTree(responseEntity);
			if (responseNode.has(PLATFORM_ID)) {
				platformId = responseNode.get(PLATFORM_ID).asText();
			}
			if(responseNode.has(CONTAINER_ID)) {
				containerId = responseNode.get(CONTAINER_ID).asText();
			}
		} catch (IOException e) {
			// swallow the exception, because the platformId is an optional parameter and
			// not
			// only relevant when linking the local and remote execution on the platform
			logger.warn("Error parsing the response from the server. The response should be a json.");
		}

		String responseValue = response.getStatus() + " " + responseEntity;

		logger.info("PLATFORMID" + CSV_SEPARATOR + uuid + CSV_SEPARATOR +  platformId);
		logger.info("CONTAINERID" + CSV_SEPARATOR + uuid + CSV_SEPARATOR +  containerId);

		return responseValue;
	}

}