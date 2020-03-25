package de.uniba.dsg.serverless.benchmark.methods;

import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BenchmarkMethods {


    /**
     * This method is used to create unique names on platform and later for invoking the function endpoints.
     * The left part of the return value is the identifier and also used for deploying the stuff on the platform.
     * The right part is the configured memory setting on the platform, if present.
     *
     * @return
     */
    public List<Pair<String, Integer>> generateFunctionNames();

    /**
     * Creates a list of url endpoints on the corresponding platform for invoking functions.
     *
     * @return
     */
    public List<String> getUrlEndpointsOnPlatform();

    /**
     * Creates a map of header parameters for a single benchmark.
     * The assumed architecture is that a single benchmark with multiple functions
     * is secured by a single secret.
     *
     * @return header map for configuring the web request
     */
    public Map<String, String> getHeaderParameter();

    /**
     * Return the platform name for logging.
     *
     * @return
     */
    public String getPlatform();

    /**
     * Only initialized, when the generated system values are present, e.g. the
     * deployment internals. Otherwise not deployed or initialized.
     *
     * @return
     */
    public boolean isInitialized();

    public void writePerformanceDataToFile(Path path, final LocalDateTime startTime, final LocalDateTime endTime) throws SeMoDeException;

    public void deploy();

    public void undeploy();
}
