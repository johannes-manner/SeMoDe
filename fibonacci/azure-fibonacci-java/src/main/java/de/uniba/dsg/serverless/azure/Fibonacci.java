package de.uniba.dsg.serverless.azure;

import com.microsoft.azure.serverless.functions.ExecutionContext;
import com.microsoft.azure.serverless.functions.HttpRequestMessage;
import com.microsoft.azure.serverless.functions.HttpResponseMessage;
import com.microsoft.azure.serverless.functions.annotation.*;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Fibonacci {

	@FunctionName("fibonacci-java")
	public HttpResponseMessage<String> handleRequest(
			@HttpTrigger(name = "req", methods = { "get",
					"post" }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			InputParameters input, @BindingName("n") String nQuery, final ExecutionContext context) {

		String nBody = input.getN();
		String invocationId = context.getInvocationId();

		String nString = "";
		if (nQuery != null && !nQuery.isEmpty()) {
			nString = nQuery;
		} else if (nBody != null && !nBody.isEmpty()) {
			nString = nBody;
		}

		if (!isNumeric(nString)) {
			return request.createResponse(400, createResponseMessage("Please pass a valid number 'n'.", invocationId));
		}

		long n = Long.parseLong(nString);
		long result = fibonacci(n);

		return request.createResponse(200, createResponseMessage(String.valueOf(result), invocationId));
	}

	public long fibonacci(long n) {
		if (n <= 1) {
			return 1;
		} else {
			return fibonacci(n - 1) + fibonacci(n - 2);
		}
	}

	public String createResponseMessage(String message, String requestId) {
		Response response = new Response(message, requestId);

		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			return "{ \"result\": \"Error while creating JSON response.\" }";
		}
	}

	public boolean isNumeric(String value) {
		return value.matches("\\d+");
	}
}
