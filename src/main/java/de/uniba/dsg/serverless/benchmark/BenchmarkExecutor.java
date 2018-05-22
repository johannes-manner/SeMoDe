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

public class BenchmarkExecutor {

	private static URL url;
	private TimeUnit TIME_UNIT = TimeUnit.SECONDS;

	/**
	 * Creates a new BenchmarkExecutor.
	 * 
	 * @param url
	 * @param numberOfRequests
	 */
	public BenchmarkExecutor(URL url) {
		BenchmarkExecutor.url = url;
	}

	/**
	 * Executes the benchmark in mode {@link BenchmarkMode#CONCURRENT}<br>
	 * This mode executes the function numberOfRequests times without delay.
	 * 
	 * @param numberOfRequests
	 * @return number of failed requests
	 */
	public int executeConcurrentBenchmark(int numberOfRequests) {
		ExecutorService executorService = Executors.newCachedThreadPool();

		List<Future<String>> responses = new ArrayList<>();
		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(new FunctionTrigger(url));
			responses.add(future);
		}

		shutdownExecutorAndAwaitTermination(executorService, 5);

		int failedRequests = 0;
		for (Future<String> future : responses) {
			try {
				future.get();
			} catch (CancellationException | ExecutionException | InterruptedException e) {
				failedRequests++;
			}
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
	 *            delays between starts of function executions in minutes
	 * @return number of failed requests
	 */
	public int executeSequentialIntervalBenchmark(int numberOfRequests, int delay) {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		List<Future<String>> responses = new ArrayList<>();
		for (int i = 0; i < numberOfRequests; i++) {
			// schedule function execution at (T + i * delay) minutes
			responses.add(executorService.schedule(new FunctionTrigger(url), i * delay, TIME_UNIT));
		}
		shutdownExecutorAndAwaitTermination(executorService, 5);

		int failedRequests = 0;
		for (Future<String> future : responses) {
			try {
				future.get();
			} catch (CancellationException | ExecutionException | InterruptedException e) {
				failedRequests++;
			}
		}
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
	 *            minutes
	 * @return number of failed requests
	 */
	public int executeSequentialWaitBenchmark(int numberOfRequests, int delay) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		int failedRequests = 0;
		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(new FunctionTrigger(url));
			try {
				future.get();
			} catch (CancellationException | ExecutionException | InterruptedException e) {
				failedRequests++;
			}
			try {
				TIME_UNIT.sleep(delay);
			} catch (InterruptedException ignored) {
			}
		}
		shutdownExecutorAndAwaitTermination(executorService, 1);
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
	 *            execution g+1 in minutes
	 * @return number of failed requests
	 */
	public int executeSequentialConcurrentBenchmark(int numberOfGroups, int numberOfRequestsEachGroup, int delay) {
		int failedRequests = 0;
		for (int burst = 0; burst < numberOfGroups; burst++) {
			failedRequests += executeConcurrentBenchmark(numberOfRequestsEachGroup);
			try {
				TIME_UNIT.sleep(delay);
			} catch (InterruptedException ignored) {
			}
		}
		return failedRequests;
	}

	/**
	 * Waits for the Executor to shut down; After the maximum wait time, the
	 * Executor is forced to shutdown immediately.
	 * 
	 * @param executor
	 * @param maxWaitTime
	 *            the maximum time to wait in minutes
	 */
	private void shutdownExecutorAndAwaitTermination(ExecutorService executorService, int maxWaitTime) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(maxWaitTime, TimeUnit.MINUTES)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
	}

}
