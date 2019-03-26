package de.uniba.dsg.serverless.benchmark;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uniba.dsg.serverless.cli.BenchmarkUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkSequentialConcurrentTest extends BenchmarkUtilityTest{
	
	@Test
	public void simpleSequentialConcurrentTest() throws SeMoDeException, URISyntaxException {
		List<String> args = Arrays.asList(this.url,
					this.jsonInput, "sequentialConcurrent", "15", "2", "15", "5");
		BenchmarkUtility utility = new BenchmarkUtility("benchmark");
		int failedRequests = utility.executeBenchmark(args);

		Assert.assertEquals(failedRequests, 0);
	}

}
