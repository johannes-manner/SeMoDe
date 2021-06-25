package de.uniba.dsg.serverless.pipeline.repo.projection;

public interface IBenchmarkVersionAggregate {

    public Integer getId();
    
    public Integer getVersionNumber();

    public Integer getLocalEvents();

    public Integer getProviderEvents();
}
