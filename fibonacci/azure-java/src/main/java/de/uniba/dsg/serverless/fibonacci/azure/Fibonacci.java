package de.uniba.dsg.serverless.fibonacci.azure;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.*;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Fibonacci {
	
	private static final String CONTAINER_ID = UUID.randomUUID().toString();

	@FunctionName("fibonacci-java")
	public HttpResponseMessage<String> handleRequest(
			@HttpTrigger(name = "req", methods = { "get", "post" }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			InputParameters input, final ExecutionContext context) {

		String nBody = input.getN();

		if (nBody == null || !isNumeric(nBody)) {
			return request.createResponse(400,
					createResponseMessage("Please pass a valid number 'n'.", context.getInvocationId()));
		}

		long n = Long.parseLong(nBody);
		long result = fibonacci(n);

		return request.createResponse(200, createResponseMessage(String.valueOf(result), context.getInvocationId()));
	}

	public HttpResponseMessage<String> handleRequest(
			@HttpTrigger(name = "req", methods = { "get", "post" }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			@BindingName("n") String nQuery, final ExecutionContext context) {

		if (nQuery == null || !isNumeric(nQuery)) {
			return request.createResponse(400,
					createResponseMessage("Please pass a valid number 'n'.", context.getInvocationId()));
		}

		long n = Long.parseLong(nQuery);
		long result = fibonacci(n);

		return request.createResponse(200, createResponseMessage(String.valueOf(result), context.getInvocationId()));
	}

	public long fibonacci(long n) {
		if (n <= 1) {
			return 1;
		} else {
			return fibonacci(n - 1) + fibonacci(n - 2);
		}
	}

	public String createResponseMessage(String message, String requestId) {
		Response response = new Response(message, requestId, CONTAINER_ID);

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
