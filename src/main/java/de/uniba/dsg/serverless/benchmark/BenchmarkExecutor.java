package de.uniba.dsg.serverless.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.io.CharStreams;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkExecutor {

	private static URL url;
	private int numberOfRequests;

	public BenchmarkExecutor(URL url, int numberOfRequests) {
		BenchmarkExecutor.url = url;
		this.numberOfRequests = numberOfRequests;
	}

	/**
	 * Executes a benchmark in one of three modes:<br>
	 * {@link BenchmarkMode#CONCURRENT}<br>
	 * Executes the function numberOfRequests times without delay.
	 * <p>
	 * {@link BenchmarkMode#SEQUENTIAL_INTERVAL}<br>
	 * Executes the function at the given fixed rate. The first execution will
	 * commence immediately. Further ones will be executed at <math>(T +
	 * delay)</math>, <math>(T + 2 * delay)</math> and so on. If a function
	 * execution exceeds the delay, further executions are delayed and not executed
	 * concurrently.
	 * <p>
	 * {@link BenchmarkMode#SEQUENTIAL_INTERVAL}<br>
	 * Executes the function at the given delay after the function execution time.
	 * The first execution will commence immediately. Furhter ones will be executed
	 * at <math>(T + executionTime1 + delay)</math> and so on.
	 * 
	 * @param delay
	 *            delay between function executions (0 for concurrent)
	 * @param mode
	 *            Mode of the Benchmark
	 * @throws SeMoDeException
	 */
	public void executeBenchmark(int delay, BenchmarkMode mode) throws SeMoDeException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		switch (mode) {
		case CONCURRENT:
			executeConcurrentBenchmark();
			return;
		case SEQUENTIAL_INTERVAL:
			System.out.println("start");
			ScheduledFuture<?> handle = executorService.scheduleAtFixedRate(BenchmarkExecutor::triggerFunction, 0,
					delay, TimeUnit.SECONDS);

			long totalExecutionTime = numberOfRequests * delay;
			executorService.schedule(new Runnable() {
				public void run() {
					handle.cancel(true);
				}
			}, totalExecutionTime, TimeUnit.SECONDS);
			
			executorService.shutdown();
			try {
				System.out.println("Executor terminated in time = "
						+ executorService.awaitTermination(totalExecutionTime * 5, TimeUnit.SECONDS));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case SEQUENTIAL_WAIT:
			for (int i = 0; i < numberOfRequests; i++) {
				triggerFunction();
				try {
					TimeUnit.SECONDS.sleep(delay);
				} catch (InterruptedException e) {
				}
			}
			break;
		default:
			return;
		}
	}
	
	private int executeConcurrentBenchmark() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		List<Future<String>> responses = new ArrayList<>();
		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(new FunctionTrigger(url));
			responses.add(future);
		}
		
		shutdownExecutorAndAwaitTermination(executorService);

		int failedRequests = 0;
		for(Future<String> future : responses) {
			try {
				System.out.println(future.get());
			} catch (ExecutionException | InterruptedException e) {
				failedRequests++;
			}
		}
		
		return failedRequests;
	}
	
	/**
	 * waits 300 sec for the Executor to shutdown; 
	 * otherwise the Executor is forced to shutdown immediately.
	 * @param executor
	 */
	private void shutdownExecutorAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(300, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
	}

	private static void triggerFunction() throws RuntimeException {
		try {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write("5");
			osw.flush();
			osw.close();
			os.close();
			con.connect();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				System.out.println("Response: " + CharStreams.toString(in));
				return;
			}
		} catch (IOException e) {

			System.err.println(e.getMessage());
			throw new RuntimeException("Function execution was not possible.", e);
		}
	}

}
