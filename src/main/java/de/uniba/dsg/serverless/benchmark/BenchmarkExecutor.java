package de.uniba.dsg.serverless.benchmark;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.Uninterruptibles;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkExecutor {

	protected static final int PLATFORM_FUNCTION_TIMEOUT = 300;

	private static final Logger logger = LogManager.getLogger(BenchmarkExecutor.class);

	private final URL url;
	private final String jsonInput;

	/**
	 * Creates a BenchmarkExecutor based on the URL and a json representation of the
	 * used data.
	 * 
	 * @param url
	 * @param jsonInput
	 */
	public BenchmarkExecutor(URL url, String jsonInput) {
		this.url = url;
		this.jsonInput = jsonInput;
		logger.debug("Created BenchmarkExecutor" + url.toString() + " - " + jsonInput);
	}

	/**
	 * Executes the benchmark in mode {@link BenchmarkMode#CONCURRENT}<br>
	 * This mode executes the function numberOfRequests times without delay.
	 * 
	 * @param numberOfRequests
	 * @return number of failed requests
	 * @throws SeMoDeException
	 */
	public int executeConcurrentBenchmark(int numberOfRequests) throws SeMoDeException {
		ExecutorService executorService = Executors.newCachedThreadPool();

		List<Future<String>> responses = new ArrayList<>();
		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(new FunctionTrigger(jsonInput, url));
			responses.add(future);
		}

		// twice the timeout to get consider scaling costs adequately
		shutdownExecutorAndAwaitTermination(executorService, PLATFORM_FUNCTION_TIMEOUT * 2);

		int failedRequests = 0;
		for (Future<String> future : responses) {
			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);
		}
		return failedRequests;
	}

	/**
	 * Executes the benchmark in mode {@link BenchmarkMode#SEQUENTIAL_INTERVAL}<br>
	 * This mode executes the function at the given fixed rate. The first execution
	 * will commence immediately. Further ones will be executed at <math>(T +
	 * delay)</math>, <math>(T + 2 * delay)</math> and so on.
	 * 
	 * @param numberOfRequests
	 * @param delay
	 *            delays between starts of function executions in seconds
	 * @return number of failed requests
	 * @throws SeMoDeException
	 */
	public int executeSequentialIntervalBenchmark(int numberOfRequests, int delay) throws SeMoDeException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		List<Future<String>> responses = new ArrayList<>();
		for (int i = 0; i < numberOfRequests; i++) {
			// schedule function execution at (T + i * delay) seconds
			responses.add(executorService.schedule(new FunctionTrigger(jsonInput, url), i * delay, TimeUnit.SECONDS));
		}

		int failedRequests = 0;
		for (Future<String> future : responses) {
			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);
		}

		shutdownExecutorAndAwaitTermination(executorService, 0);

		return failedRequests;
	}

	/**
	 * Executes the Benchmark in mode {@link BenchmarkMode#SEQUENTIAL_WAIT}. <br>
	 * This mode executes the function at the given delay after the function
	 * execution time. The first execution will commence immediately. Furhter ones
	 * will be executed at <math>(T + executionTimeLast + delay)</math> and so on.
	 * <p>
	 * 
	 * @param numberOfRequests
	 * @param delay
	 *            between the end of execution n and the start of execution n+1 in
	 *            seconds
	 * @return number of failed requests
	 * @throws SeMoDeException
	 */
	public int executeSequentialWaitBenchmark(int numberOfRequests, int delay) throws SeMoDeException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		int failedRequests = 0;
		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(new FunctionTrigger(jsonInput, url));
			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);
			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
		}
		shutdownExecutorAndAwaitTermination(executorService, 0);
		return failedRequests;
	}

	/**
	 * Executes the Benchmark in mode {@link BenchmarkMode#SEQUENTIAL_CONCURRENT}.
	 * <br>
	 * The mode executes functions in groups of concurrent requests. The group
	 * executions are delayed, group g + 1 will start after group g terminated +
	 * delay.
	 * 
	 * @param numberOfGroups
	 * @param numberOfRequestsEachGroup
	 * @param delay
	 *            between the end of group execution g and the start of group
	 *            execution g+1 in seconds
	 * @return number of failed requests
	 * @throws SeMoDeException
	 */
	public int executeSequentialConcurrentBenchmark(int numberOfGroups, int numberOfRequestsEachGroup, int delay)
			throws SeMoDeException {
		int failedRequests = 0;
		for (int burst = 0; burst < numberOfGroups; burst++) {
			failedRequests += executeConcurrentBenchmark(numberOfRequestsEachGroup);
			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
		}
		return failedRequests;
	}

	/**
	 * Executes the Benchmark in mode
	 * {@link BenchmarkMode#SEQUENTIAL_CHANGING_INTERVAL}. <br>
	 * Similar to mode {@link BenchmarkMode#SEQUENTIAL_INTERVAL}, functions get
	 * executed with a varying delay between the execution starts. The first
	 * execution will commence immediately, subsequent ones will be delayed at the
	 * specified delays in <code>delays</code>.
	 * 
	 * @param numberOfRequests
	 *            the number of total requests
	 * @param delays
	 *            delays of requests
	 * @return number of failed requests
	 */
	public int executeSequentialChangingIntervalBenchmark(int numberOfRequests, int[] delays) throws SeMoDeException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

		List<Future<String>> responses = new ArrayList<>();
		int time = 0;
		for (int i = 0; i < numberOfRequests; i++) {
			responses.add(executorService.schedule(new FunctionTrigger(jsonInput, url), time, TimeUnit.SECONDS));
			time += delays[i % delays.length];
		}

		int failedRequests = 0;
		for (Future<String> future : responses) {
			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);
		}

		shutdownExecutorAndAwaitTermination(executorService, 0);

		return failedRequests;
	}

	/**
	 * Executes the Benchmark in mode
	 * {@link BenchmarkMode#SEQUENTIAL_CHANGING_WAIT}. <br>
	 * Similar to mode {@link BenchmarkMode#SEQUENTIAL_WAIT}, functions get executed
	 * with a varying delay between the termination of execution n and the start of
	 * execution n+1. The first execution will commence immediately, subsequent ones
	 * will be delayed after predecessors termination by varying delays specified in
	 * <code>delays</code>.
	 * 
	 * @param numberOfRequests
	 * @param delays
	 * @return
	 * @throws SeMoDeException
	 *             If execution fails
	 */
	public int executeSequentialChangingWaitBenchmark(int numberOfRequests, int[] delays) throws SeMoDeException {

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		int failedRequests = 0;

		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(new FunctionTrigger(jsonInput, url));
			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);

			int delay = delays[i % delays.length];
			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
		}

		this.shutdownExecutorAndAwaitTermination(executorService, 0);

		return failedRequests;

	}

	private int exceptionHandlingFuture(ExecutorService executorService, int failedRequests, Future<String> future)
			throws SeMoDeException {
		try {
			while (!future.isDone()) {
				try {
					future.get();
				} catch (InterruptedException e) {
					logger.info("InterruptedException - investigate this orphan exception");
				}
			}
		} catch (CancellationException | ExecutionException e) {
			logger.warn("ExecutionException", e);
			failedRequests++;
		}
		return failedRequests;
	}

	/**
	 * waits for the Executor to shutdown; After a timeout, the Executor is forced
	 * to shutdown immediately.
	 * 
	 * @param executor
	 * @param maxWaitTime
	 *            the maximum time to wait in seconds
	 */
	private void shutdownExecutorAndAwaitTermination(ExecutorService executorService, int maxWaitTime) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(maxWaitTime, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
	}

}
