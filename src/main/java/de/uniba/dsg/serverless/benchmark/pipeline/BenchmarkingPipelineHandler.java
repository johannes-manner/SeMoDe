package de.uniba.dsg.serverless.benchmark.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class BenchmarkingPipelineHandler extends CustomUtility {
	
	protected static final String ENDPOINTS_FOLDER = "endpoints";
	protected static final String BENCHMARKING_COMMANDS_FOLDER = "benchmarking-commands";

	private static final String BENCHMARK_SETTING_FOLDER = "benchmarkSetting";
	private static final Logger logger = LogManager.getLogger(BenchmarkingPipelineHandler.class.getName());

	public BenchmarkingPipelineHandler(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {

		try {
			if (args == null || args.size() < 2) {
				logger.fatal("Input parameters are empty or corrupted");
				return;
			}

			int step = Integer.valueOf(args.get(0));
			Path pathBenchmarkingSetting = Paths.get(BENCHMARK_SETTING_FOLDER + "/" + args.get(1));
			BenchmarkingPipelineHandler.createFolderIfNotExists(pathBenchmarkingSetting);

			// before benchmark, after deployment
			if (step == 0) {

				//TODO: validation of the input arguments
				String inputFilePath = args.get(2);
				String language = args.get(3);
				String logType = args.get(4);
				String provider = args.get(5);
				String benchmarkingMode = args.get(6);		// e.g. "sequentialChangingInterval"
				String benchmarkingParameters = args.get(7);// e.g. "4 30"
				
				new EndpointExtractor(inputFilePath, pathBenchmarkingSetting, language, logType, provider).extractEndpoints();
				logger.info("Endpoint extraction succeeded!");
				new BenchmarkingCommandGenerator(pathBenchmarkingSetting, language, provider).generateCommands(benchmarkingMode, benchmarkingParameters);
				logger.info("Benchmark command generation succeeded!");
				
				// after benchmarking execution
			} else if (step == 1) {
				new FetchingCommandGenerator();
			}
		} catch (SeMoDeException e) {
			logger.fatal("An error occured. The utitily is shut down. Error message: " + e.getMessage());
		}
	}

	protected static void createFolderIfNotExists(Path path) throws SeMoDeException {
		if (!Files.isDirectory(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				throw new SeMoDeException("Could not create directory " + path.toString(), e);
			}
		}
	}
}
