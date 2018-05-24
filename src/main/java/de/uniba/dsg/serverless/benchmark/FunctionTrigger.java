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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class FunctionTrigger implements Callable<String> {
	
	// only use the logger in this class for logging the REST data - see log4j2-test.xml
	private static final Logger logger  = LogManager.getLogger(FunctionTrigger.class.getName());
	
	private static final String CSV_SEPARATOR = System.getProperty("CSV_SEPARATOR");

	// used in the response header element to map the executions on the cloud platform
	private static final String PLATFORM_ID = "platformId";
	
	private static final ObjectReader jsonReader = new ObjectMapper().reader();
		
	private final String host;
	private final String path;
	private final Map<String, String> queryParameters;
	private final String jsonInput;
	
	public FunctionTrigger(String jsonInput, URL url) {
		
		this.jsonInput = jsonInput;
		
		this.host = url.getProtocol() + "://" + url.getHost();
		this.path = url.getPath();

		this.queryParameters = new HashMap<>();
		String[] queries = url.getQuery().split("\\?");
		for (String query : queries) {
			int pos = query.indexOf('=');
			this.queryParameters.put(query.substring(0, pos), query.substring(pos + 1));
		}
	}

	@Override
	public String call() throws SeMoDeException {
		
		String uuid = UUID.randomUUID().toString();
		logger.info("START" + CSV_SEPARATOR + uuid);
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(host).path(path);

		for (String key : queryParameters.keySet()) {
			target = target.queryParam(key, queryParameters.get(key));
		}

		Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(jsonInput,
						MediaType.APPLICATION_JSON));
		
		// the response entity has to be a json representation with a platformId and a result key
		String responseEntity = response.readEntity(String.class);
		String platformId = "";
		
		try {
			JsonNode responseNode = jsonReader.readTree(responseEntity);
			platformId = responseNode.get(PLATFORM_ID).asText();
		} catch (IOException e) {
			// swallow the exception, because the platformId is an optional parameter and not 
			// only relevant when linking the local and remote execution on the platform
			logger.warn("Error parsing the response from the server. The response should be a json.");
		}
		
		
		String responseValue = response.getStatus() + " " + responseEntity;

		logger.info("END" + CSV_SEPARATOR + uuid);
		logger.info("PLATFORMID" + CSV_SEPARATOR + uuid + CSV_SEPARATOR  + platformId);
		
		return responseValue;
	}

}