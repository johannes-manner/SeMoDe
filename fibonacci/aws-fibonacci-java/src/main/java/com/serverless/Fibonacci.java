package com.serverless;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Fibonacci implements RequestHandler<Map<String, String>, Response> {

	@Override
	public Response handleRequest(Map<String, String> parameters, Context context) {
		String n_string = parameters.getOrDefault("n", "");

		if (!isNumeric(n_string)) {
			return createErrorResponse("Please pass a valid number 'n'.", context.getAwsRequestId());
		}

		long n = Integer.parseInt(n_string);
		long result = fibonacci(n);

		return createSuccessResponse(String.valueOf(result), context.getAwsRequestId());
	}

	public long fibonacci(long n) {
		if (n <= 1) {
			return 1;
		} else {
			return fibonacci(n - 1) + fibonacci(n - 2);
		}
	}

	public Response createErrorResponse(String message, String requestId) {
		Response response = new Response("[400] " + message, requestId);

		ObjectMapper mapper = new ObjectMapper();
		try {
			String responseJSON = mapper.writeValueAsString(response);
			throw new LambdaException(responseJSON);
		} catch (JsonProcessingException e) {
			throw new LambdaException("[400] Error while creating JSON response.");
		}
	}

	public Response createSuccessResponse(String message, String requestId) {
		return new Response(message, requestId);
	}

	public boolean isNumeric(String value) {
		return value.matches("\\d+");
	}

}
