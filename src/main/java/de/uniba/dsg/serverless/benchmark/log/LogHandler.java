package de.uniba.dsg.serverless.benchmark.log;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;

import java.util.Map;

public interface LogHandler {

    public Map<String, WritableEvent> getPerformanceData() throws SeMoDeException;

}
