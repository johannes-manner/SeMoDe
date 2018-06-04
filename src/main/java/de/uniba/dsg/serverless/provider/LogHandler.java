package de.uniba.dsg.serverless.provider;

import java.util.Map;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;

public interface LogHandler {

	public Map<String, WritableEvent> getPerformanceData() throws SeMoDeException;

}
