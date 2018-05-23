package de.uniba.dsg.serverless.benchmark;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class FunctionTrigger implements Callable<String> {
	
	private static final Logger logger = Logger.getLogger(FunctionTrigger.class.getName());
	
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
		logger.info("START " + uuid);

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(host).path(path);

		for (String key : queryParameters.keySet()) {
			target = target.queryParam(key, queryParameters.get(key));
		}

		Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(jsonInput,
						MediaType.APPLICATION_JSON));
		String responseValue = response.getStatus() + " " + response.getEntity();

		logger.info("END " + uuid);
		return responseValue;
	}

}