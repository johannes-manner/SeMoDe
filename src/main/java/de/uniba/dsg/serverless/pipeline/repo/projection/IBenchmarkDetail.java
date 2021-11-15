package de.uniba.dsg.serverless.pipeline.repo.projection;

public interface IBenchmarkDetail {

    public Double getMemory();

    public Double getDuration();

    public String getCpu();

    public String getName();

    public String getVm();

    public boolean getCold();
}
