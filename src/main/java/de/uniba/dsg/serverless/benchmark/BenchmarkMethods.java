package de.uniba.dsg.serverless.benchmark;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

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
     * Only initialized, when the generated system values are present, e.g. the
     * deployment internals. Otherwise not deployed or initialized.
     *
     * @return
     */
    public boolean isInitialized();

    public void deploy();

    public void undeploy();
}
