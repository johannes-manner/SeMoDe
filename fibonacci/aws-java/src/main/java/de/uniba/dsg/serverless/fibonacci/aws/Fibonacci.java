package de.uniba.dsg.serverless.fibonacci.aws;

import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Fibonacci implements RequestHandler<Map<String, String>, Response> {
	
	private static final String CONTAINER_ID = UUID.randomUUID().toString();

	@Override
	public Response handleRequest(Map<String, String> parameters, Context context) {
		String nString = parameters.getOrDefault("n", "");

		if (!isNumeric(nString)) {
			return createErrorResponse("Please pass a valid number 'n'.", context.getAwsRequestId());
		}

		long n = Long.parseLong(nString);
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
		Response response = new Response("[400] " + message, requestId, CONTAINER_ID);

		ObjectMapper mapper = new ObjectMapper();
		try {
			String responseJSON = mapper.writeValueAsString(response);
			throw new LambdaException(responseJSON);
		} catch (JsonProcessingException e) {
			throw new LambdaException("{ \"result\": \"[400] Error while creating JSON response.\" }");
		}
	}

	public Response createSuccessResponse(String message, String requestId) {
		Response response = new Response(message, requestId, CONTAINER_ID);
		response.addCPUAndVMInfo();
		return response;
	}

	public boolean isNumeric(String value) {
		return value.matches("\\d+");
	}
	
	public static void main(String[] args) {
		String s = "REPORT RequestId: 6fef688c-ae35-4407-89a8-912d5e5f27a9 Duration: 356.29 ms Billed Duration: 400 ms Memory Size: 256 MB Max Memory Used: 88 MB Init Duration: 343.95 ms " + 
				"XRAY TraceId: 1-5d71369b-1f497ff4af6ac47211675615 SegmentId: 52b5ebff3d157fa5 Sampled: false";
		
		int i = 0 ;
		for(String st : s.split(" ")) {
			System.out.println(i + " " + st);
			i++;
		}
	}

}
