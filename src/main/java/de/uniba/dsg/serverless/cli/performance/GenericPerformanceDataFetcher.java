package de.uniba.dsg.serverless.cli.performance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;
import de.uniba.dsg.serverless.provider.LogHandler;
import de.uniba.dsg.serverless.util.BenchmarkingRESTAnalyzer;

public interface GenericPerformanceDataFetcher {

	public static final double NETWORK_AND_PLATFORM_DELAY = 5000.0;

	default String generateFileName(String provider, String functionName) {
		String dateText = new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date());
		String fileName = provider + "-" + functionName + "-" + dateText + ".csv";
		return fileName;
	}

	default void writePerformanceDataToFile(String provider, LogHandler logHandler, String functionName, Optional<String> restFile)
			throws SeMoDeException {

		Map<String, WritableEvent> restMap = new HashMap<>();

		// if a benchmarking file is selected
		if (restFile.isPresent()) {
			BenchmarkingRESTAnalyzer restAnalyzer = new BenchmarkingRESTAnalyzer(Paths.get(restFile.get()));
			restMap = restAnalyzer.extractRESTEvents();
		}

		this.writePerformanceDataToFile(this.generateFileName(provider, functionName), restMap, logHandler.getPerformanceData());
	}

	default void writePerformanceDataToFile(String fileName, Map<String, WritableEvent> restMap,
			Map<String, WritableEvent> performanceProviderMap) throws SeMoDeException {
		try {
			String OUTPUT_DIRECTORY = "performanceData";

			if (!Files.exists(Paths.get(OUTPUT_DIRECTORY))) {
				Files.createDirectory(Paths.get(OUTPUT_DIRECTORY));
			}
			Path file = Files.createFile(Paths.get(OUTPUT_DIRECTORY + "/" + fileName));
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {

				// Writing header line
				for (Map<String, WritableEvent> map : Arrays.asList(restMap, performanceProviderMap)) {
					if (map.isEmpty()) {
						throw new SeMoDeException(
								"The platform map is empty. The most likely reason is the wrong start and end time.");
					} else {
						String key = map.keySet().iterator().next();
						writer.write(map.get(key).getCSVMetadata());
					}
				}
				writer.write(System.lineSeparator());

				// Writing rows
				for (String key : restMap.keySet()) {
					WritableEvent restEvent = restMap.get(key);
					writer.write(restEvent.toCSVString());

					WritableEvent providerEvent = performanceProviderMap.get(key);

					if (providerEvent != null) {
						writer.write(providerEvent.toCSVString());
					} 
					writer.write(System.lineSeparator());
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Writing to file failed");
		}
	}
}
