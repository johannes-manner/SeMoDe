package com.serverless;

import java.io.IOException;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Fibonacci implements RequestHandler<Map<String, String>, ApiGatewayResponse> {
	
	/*@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		@SuppressWarnings("unchecked")
		Map<String, String> queryParameters = (Map<String, String>) input.get("queryStringParameters");
		String body = (String) input.get("body");
		
		String n_string = "";
		if (queryParameters != null && queryParameters.containsKey("n")) {
			n_string = queryParameters.get("n");
		} else if (body != null) {
			ObjectMapper mapper = new ObjectMapper();

			try {
				JsonNode bodyJSON = mapper.readTree(body);
				
				if (bodyJSON.has("n")) {
					n_string = bodyJSON.get("n").asText();
				}
			} catch (IOException e) {
				return createErrorResponse("Please pass valid JSON as body.");
			}
		}
		
		if (!isNumeric(n_string)) {
			return createErrorResponse("Please pass a valid number 'n'.");
	    }

	    long n = Integer.parseInt(n_string);
	    long result = fibonacci(n);
	    
	    return createSuccessResponse(String.valueOf(result));
	}*/

	@Override
	public ApiGatewayResponse handleRequest(Map<String, String> parameters, Context context) {
		String n_query = parameters.get("n_query");
		String n_body = parameters.get("n_body");

		String n_string = "";
		if (!n_query.isEmpty()) {
			n_string = n_query;
		} else if (!n_body.isEmpty()) {
			n_string = n_body;
		}
		
	    if (!isNumeric(n_string)) {
	        return createErrorResponse("Please pass a valid number 'n'.");
	    }

	    long n = Integer.parseInt(n_string);
	    long result = fibonacci(n);
	    
	    return createSuccessResponse(String.valueOf(result));
	}

	public long fibonacci(long n) {
		if (n <= 1) {
			return 1;
		} else {
			return fibonacci(n - 1) + fibonacci(n - 2);
		}
	}
	
	public ApiGatewayResponse createErrorResponse(String message) {
	    return ApiGatewayResponse.builder().setStatusCode(400).setObjectBody(message).build();
	}
	
	public ApiGatewayResponse createSuccessResponse(String message) {
	    return ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(message).build();
	}
	
	public boolean isNumeric(String value) {
		return value.matches("\\d+");
	}

}
