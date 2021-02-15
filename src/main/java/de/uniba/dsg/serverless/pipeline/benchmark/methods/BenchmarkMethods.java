package de.uniba.dsg.serverless.pipeline.benchmark.methods;

import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BenchmarkMethods {

    /**
     * This method is used to create unique names on platform and later for invoking the function endpoints. The left
     * part of the return value is the identifier and also used for deploying the stuff on the platform. The right part
     * is the configured memory setting on the platform, if present.
     */
    public List<Pair<String, Integer>> generateFunctionNames();

    /**
     * Creates a list of url endpoints on the corresponding platform for invoking functions.
     */
    public List<String> getUrlEndpointsOnPlatform();

    /**
     * Creates a map of header parameters for a single benchmark. The assumed architecture is that a single benchmark
     * with multiple functions is secured by a single secret.
     *
     * @return header map for configuring the web request
     */
    public Map<String, String> getHeaderParameter();

    /**
     * Return the platform name for logging.
     */
    public String getPlatform();

    /**
     * Only initialized, when the generated system values are present, e.g. the deployment internals. Otherwise not
     * deployed or initialized.
     */
    public boolean isInitialized();

    public List<PerformanceData> getPerformanceDataFromPlatform(final LocalDateTime startTime, final LocalDateTime endTime) throws SeMoDeException;

    public void deploy();

    public void undeploy();
}
