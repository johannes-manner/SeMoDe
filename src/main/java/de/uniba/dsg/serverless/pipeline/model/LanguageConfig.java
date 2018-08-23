package de.uniba.dsg.serverless.pipeline.model;

public class LanguageConfig {

	private String identifier;
	private String logType;
	private String fetcherType;

	public LanguageConfig() {
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getFetcherType() {
		return fetcherType;
	}

	public void setFetcherType(String fetcherType) {
		this.fetcherType = fetcherType;
	}

	@Override
	public String toString() {
		return "LanguageConfig [identifier=" + identifier + ", logType=" + logType + ", fetcherType=" + fetcherType
				+ "]";
	}

}
