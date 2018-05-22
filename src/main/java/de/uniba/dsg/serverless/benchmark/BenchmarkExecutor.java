package de.uniba.dsg.serverless.benchmark;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.istack.logging.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkExecutor {
	
	private static final int PLATFORM_FUNCTION_TIMEOUT = 300;

	private static final Logger logger = Logger.getLogger(BenchmarkExecutor.class);

	private final String host;
	private final String path;
	private final String jsonInput;
	private final int numberOfRequests;
	private final Map<String, String> queryParameters;
	
	public BenchmarkExecutor(String urlString, String path, String jsonInput, int numberOfRequests) throws MalformedURLException {
		URL url = new URL(urlString);
		this.host = url.getProtocol() + "://" + url.getHost();
		this.path = url.getPath();
		
		this.queryParameters = new HashMap<>();
		String[] queries = url.getQuery().split("\\?");
		for(String query : queries) {
			int pos = query.indexOf('=');
			this.queryParameters.put(query.substring(0, pos), query.substring(pos+1));
		}
		this.jsonInput = jsonInput;
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
		switch (mode) {
		case CONCURRENT:
			this.executeConcurrentBenchmark();
			break;
		case SEQUENTIAL_INTERVAL:
			this.executeSequentialInterval(delay);
			break;
		case SEQUENTIAL_WAIT:
			this.executeSequentialWait(delay);
			break;
		default:
			return;
		}
	}

	/**
	 * 
	 * 
	 * @param delay
	 * @throws SeMoDeException
	 */
	private void executeSequentialWait(int delay) throws SeMoDeException {
		for (int i = 0; i < numberOfRequests; i++) {
			try {
				String result = this.createFunctionTrigger().call();
				logger.info(result);
			} catch (Exception callException) {
				logger.warning("An error occured while executing the callable.");
				throw new SeMoDeException("An error occured while executing the callable.", callException);
			}
			
			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
		}
	}

	private void executeSequentialInterval(int delay) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		List<Future<String>> responses = new ArrayList<>();
		for(int i = 0 ; i < numberOfRequests ; i++) {
			Future<String> future = executorService.submit(this.createFunctionTrigger());
			responses.add(future);
			
			Uninterruptibles.sleepUninterruptibly(delay, TimeUnit.SECONDS);
		}
		
		shutdownExecutorAndAwaitTermination(executorService);
		
		for (Future<String> future : responses) {
			try {
				System.out.println(future.get());
			} catch (ExecutionException | InterruptedException e) {
				System.out.println("Execution failed due to an error.");
			}
		}
	}

	private int executeConcurrentBenchmark() {
		ExecutorService executorService = Executors.newCachedThreadPool();

		List<Future<String>> responses = new ArrayList<>();
		for (int i = 0; i < numberOfRequests; i++) {
			Future<String> future = executorService.submit(this.createFunctionTrigger());
			responses.add(future);
		}

		shutdownExecutorAndAwaitTermination(executorService);

		int failedRequests = 0;
		for (Future<String> future : responses) {
			try {
				System.out.println(future.get());
			} catch (ExecutionException | InterruptedException e) {
				failedRequests++;
			}
		}

		return failedRequests;
	}

	/**
	 * waits 300 sec for the Executor to shutdown; otherwise the Executor is forced
	 * to shutdown immediately.
	 * 
	 * @param executor
	 */
	private void shutdownExecutorAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(PLATFORM_FUNCTION_TIMEOUT, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
	}
	
	
	private Callable<String> createFunctionTrigger(){
		return new Callable<String>() {
			public String call() {
				
				String uuid = UUID.randomUUID().toString();
				logger.info("START " + uuid);
				
				Client client = ClientBuilder.newClient();
				WebTarget target = client.target(host)
						.path(path);
				
				for(String key : queryParameters.keySet()) {
					target = target.queryParam(key, queryParameters.get(key));
				}
				
				Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
						.post(Entity.entity("{\r\n" + 
								"    \"name\": \"Azure\",\r\n" + 
								"    \"number\": 41\r\n" + 
								"}", MediaType.APPLICATION_JSON));
				String responseValue = response.getStatus() + " " + response.getEntity();
				
				logger.info("END " + uuid);
				return responseValue;
			}
		};
	}
}
