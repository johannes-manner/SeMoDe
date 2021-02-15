package de.uniba.dsg.serverless.pipeline.benchmark.provider;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

import java.util.List;

public interface LogHandler {

    public List<PerformanceData> getPerformanceData() throws SeMoDeException;
}
