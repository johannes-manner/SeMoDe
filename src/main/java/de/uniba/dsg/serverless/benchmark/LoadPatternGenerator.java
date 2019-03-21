package de.uniba.dsg.serverless.benchmark;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.Uninterruptibles;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class LoadPatternGenerator {

	protected static final int PLATFORM_FUNCTION_TIMEOUT = 300;

	private static final Logger logger = LogManager.getLogger(LoadPatternGenerator.class);

	public LoadPatternGenerator() {
		
	}


//
//	/**
//	 * Executes the Benchmark in mode {@link BenchmarkMode#SEQUENTIAL_CONCURRENT}.
//	 * <br>
//	 * The mode executes functions in groups of concurrent requests. The group
//	 * executions are delayed, group g + 1 will start after group g terminated +
//	 * delay.
//	 * 
//	 * @param numberOfGroups
//	 * @param numberOfRequestsEachGroup
//	 * @param delay
//	 *            between the end of group execution g and the start of group
//	 *            execution g+1 in seconds
//	 * @return number of failed requests
//	 * @throws SeMoDeException
//	 */
//	public int executeSequentialConcurrentBenchmark(int numberOfGroups, int numberOfRequestsEachGroup, int delay)
//			throws SeMoDeException {
//		int failedRequests = 0;
//		for (int burst = 0; burst < numberOfGroups; burst++) {
//			failedRequests += executeConcurrentBenchmark(numberOfRequestsEachGroup);
//			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
//		}
//		return failedRequests;
//	}
//
//	/**
//	 * Executes the Benchmark in mode
//	 * {@link BenchmarkMode#SEQUENTIAL_CHANGING_INTERVAL}. <br>
//	 * Similar to mode {@link BenchmarkMode#SEQUENTIAL_INTERVAL}, functions get
//	 * executed with a varying delay between the execution starts. The first
//	 * execution will commence immediately, subsequent ones will be delayed at the
//	 * specified delays in <code>delays</code>.
//	 * 
//	 * @param numberOfRequests
//	 *            the number of total requests
//	 * @param delays
//	 *            delays of requests
//	 * @return number of failed requests
//	 */
//	public int executeSequentialChangingIntervalBenchmark(int numberOfRequests, int[] delays) throws SeMoDeException {
//		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//
//		List<Future<String>> responses = new ArrayList<>();
//		int time = 0;
//		for (int i = 0; i < numberOfRequests; i++) {
//			responses.add(executorService.schedule(new FunctionTrigger(jsonInput, url), time, TimeUnit.SECONDS));
//			time += delays[i % delays.length];
//		}
//
//		int failedRequests = 0;
//		for (Future<String> future : responses) {
//			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);
//		}
//
//		shutdownExecutorAndAwaitTermination(executorService, 0);
//
//		return failedRequests;
//	}
//
//	/**
//	 * Executes the Benchmark in mode
//	 * {@link BenchmarkMode#SEQUENTIAL_CHANGING_WAIT}. <br>
//	 * Similar to mode {@link BenchmarkMode#SEQUENTIAL_WAIT}, functions get executed
//	 * with a varying delay between the termination of execution n and the start of
//	 * execution n+1. The first execution will commence immediately, subsequent ones
//	 * will be delayed after predecessors termination by varying delays specified in
//	 * <code>delays</code>.
//	 * 
//	 * @param numberOfRequests
//	 * @param delays
//	 * @return
//	 * @throws SeMoDeException
//	 *             If execution fails
//	 */
//	public int executeSequentialChangingWaitBenchmark(int numberOfRequests, int[] delays) throws SeMoDeException {
//
//		ExecutorService executorService = Executors.newSingleThreadExecutor();
//		int failedRequests = 0;
//
//		for (int i = 0; i < numberOfRequests; i++) {
//			Future<String> future = executorService.submit(new FunctionTrigger(jsonInput, url));
//			failedRequests = exceptionHandlingFuture(executorService, failedRequests, future);
//
//			int delay = delays[i % delays.length];
//			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
//		}
//
//		this.shutdownExecutorAndAwaitTermination(executorService, 0);
//
//		return failedRequests;
//
//	}

	
	

	public Path generateConcurrentLoadPattern(List<String> args) throws SeMoDeException {
		List<Double> timestamps = new ArrayList<>();
		int numberOfRequests = Integer.parseInt(args.get(4));
		
		for( int i = 0 ; i < numberOfRequests ; i++ ) {
			timestamps.add(0.0);
		}
		
		return this.writeLoadPatternToFile(timestamps, BenchmarkMode.CONCURRENT.getText());
	}
	
	public Path generateSequentialInterval(List<String> args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private Path writeLoadPatternToFile(List<Double> timestamps, String fileName) throws SeMoDeException {
		try {
			List<String> lines = timestamps.stream().map(d -> "" + d).collect(Collectors.toList());
			return Files.write(Paths.get(fileName + ".csv"), lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new SeMoDeException("Was unable to write the load pattern csv.", e);
		}
	}

	

}
