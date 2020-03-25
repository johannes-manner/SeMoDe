package de.uniba.dsg.serverless.pipeline.benchmark.log;

import de.uniba.dsg.serverless.pipeline.benchmark.model.WritableEvent;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.util.Map;

public interface LogHandler {

    public Map<String, WritableEvent> getPerformanceData() throws SeMoDeException;

}
