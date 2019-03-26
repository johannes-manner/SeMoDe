package de.uniba.dsg.serverless.benchmark;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

import de.uniba.dsg.serverless.cli.BenchmarkUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkArbitraryLoadTest extends BenchmarkUtilityTest {

	@Test
	public void simpleArbitraryLoadTest() throws SeMoDeException, URISyntaxException {
		List<String> args = Arrays.asList(this.url, this.jsonInput, "arbitraryLoadPattern", "15", Paths.get(Resources.getResource("timestamps.csv").toURI()).toString());
		BenchmarkUtility utility = new BenchmarkUtility("benchmark");
		int failedRequests = utility.executeBenchmark(args);

		Assert.assertEquals(failedRequests, 0);
	}
}
