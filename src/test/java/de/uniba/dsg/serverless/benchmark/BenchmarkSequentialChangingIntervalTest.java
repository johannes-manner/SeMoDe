package de.uniba.dsg.serverless.benchmark;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uniba.dsg.serverless.cli.BenchmarkUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkSequentialChangingIntervalTest extends BenchmarkUtilityTest {

	@Test
	public void simpleConcurrentTest() throws SeMoDeException, URISyntaxException {
		List<String> args = Arrays.asList(this.url,
					this.jsonInput, "sequentialChangingInterval", "15", "10", "1", "2", "1");
		BenchmarkUtility utility = new BenchmarkUtility("benchmark");
		int failedRequests = utility.executeBenchmark(args);

		Assert.assertEquals(failedRequests, 0);
	}
}
