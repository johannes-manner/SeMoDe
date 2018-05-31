package com.serverless;

public class Response {

	private final String result;
	private final String platformId;

	public Response(String result, String platformId) {
		this.result = result;
		this.platformId = platformId;
	}

	public String getResult() {
		return this.result;
	}

	public String getPlatformId() {
		return this.platformId;
	}
}
