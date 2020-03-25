package de.uniba.dsg.serverless.pipeline.model.config;

public class LanguageConfig {

    private String identifier;
    private String logType;
    private String fetcherType;

    public LanguageConfig() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String getLogType() {
        return this.logType;
    }

    public void setLogType(final String logType) {
        this.logType = logType;
    }

    public String getFetcherType() {
        return this.fetcherType;
    }

    public void setFetcherType(final String fetcherType) {
        this.fetcherType = fetcherType;
    }

    @Override
    public String toString() {
        return "LanguageConfig [identifier=" + this.identifier + ", logType=" + this.logType + ", fetcherType=" + this.fetcherType
                + "]";
    }

}
