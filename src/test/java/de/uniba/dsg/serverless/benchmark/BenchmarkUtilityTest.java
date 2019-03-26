package de.uniba.dsg.serverless.benchmark;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.uniba.dsg.serverless.ArgumentProcessor;

public abstract class BenchmarkUtilityTest {

	private HttpServer server;
	private final int port;
	protected String url;
	protected String jsonInput;
	
	
	protected BenchmarkUtilityTest() {
		
		ArgumentProcessor.initLog4JParameters(null);
		Logger logger = LogManager.getLogger(BenchmarkUtilityTest.class.getName());
		
		port = 12345;
		url = "http://localhost:" + port + "/benchmark";
		try {
			jsonInput = Paths.get(Resources.getResource("params.json").toURI()).toString();
		} catch (URISyntaxException e) {
			logger.warn(e.getMessage());
		}
	}

	@Before
	public void setup() throws IOException {

		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/benchmark", new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				String response = "{ \"platformId\": \"" + UUID.randomUUID() + "\" , \"containerId\": \"" + Thread.currentThread().getId() + "\"}";
				exchange.sendResponseHeaders(200, response.length());
				try (OutputStream os = exchange.getResponseBody()) {
					os.write(response.getBytes());
					os.flush();
				}
			}

		});
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
	}

	@After
	public void shutdown() {
		server.stop(0);
	}
}
