package de.uniba.dsg.serverless.pipeline.benchmark.log;

import java.util.List;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.util.SeMoDeException;

public interface LogHandler {

    public List<PerformanceData> getPerformanceData() throws SeMoDeException;
}
