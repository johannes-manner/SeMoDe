package de.uniba.dsg.serverless.pipeline.benchmark.model;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FunctionTriggerWrapper implements Runnable {

    private final ExecutorService delegator;
    private final List<Future<LocalRESTEvent>> responses;
    private final FunctionTrigger f;

    public FunctionTriggerWrapper(ExecutorService delegator, List<Future<LocalRESTEvent>> responses, FunctionTrigger f) {
        this.delegator = delegator;
        this.responses = responses;
        this.f = f;
    }

    @Override
    public void run() {
        this.responses.add(this.delegator.submit(this.f));
    }
}
