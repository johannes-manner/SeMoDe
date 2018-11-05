package de.uniba.dsg.serverless.fibonacci.azure;

public class Response {

	private final String result;
	private final String platformId;
	private final String containerId;

	public Response(String result, String platformId, String containerId) {
		this.result = result;
		this.platformId = platformId;
		this.containerId = containerId;
	}

	public String getResult() {
		return this.result;
	}

	public String getPlatformId() {
		return this.platformId;
	}
	
	public String getContainerId() {
		return this.containerId;
	}
}

