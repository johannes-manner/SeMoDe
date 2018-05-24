package de.uniba.dsg.serverless.azure;

import java.io.IOException;
import java.util.*;
import com.microsoft.azure.serverless.functions.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.serverless.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Fibonacci {
	@FunctionName("fibonacci-java")
	public HttpResponseMessage<String> handleRequest(
			@HttpTrigger(name = "req", methods = { "get",
					"post" }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) {

		Optional<String> bodyOptional = request.getBody();

		String n_string = "";
		if (request.getQueryParameters().containsKey("n")) {
			n_string = request.getQueryParameters().get("n");
		} else if (bodyOptional.isPresent()) {
			String body = bodyOptional.get();
			ObjectMapper mapper = new ObjectMapper();

			try {
				JsonNode bodyJSON = mapper.readTree(body);

				if (bodyJSON.has("n")) {
					n_string = bodyJSON.get("n").asText();
				}
			} catch (IOException e) {
				return request.createResponse(400, "Please pass valid JSON as body.");
			}
		}
		
		if (!isNumeric(n_string)) {
			return request.createResponse(400, "Please pass a valid number 'n'.");
	    }

	    long n = Long.parseLong(n_string);
	    long result = fibonacci(n);
	    
	    return request.createResponse(200, String.valueOf(result));
	}

	public long fibonacci(long n) {
		if (n <= 1) {
			return 1;
		} else {
			return fibonacci(n - 1) + fibonacci(n - 2);
		}
	}

	public boolean isNumeric(String value) {
		return value.matches("\\d+");
	}
}
