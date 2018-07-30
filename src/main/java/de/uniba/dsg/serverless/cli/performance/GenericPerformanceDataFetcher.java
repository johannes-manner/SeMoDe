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

	default String generateFileName(String functionName) {
		String dateText = new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date());
		String fileName = functionName + "-" + dateText + ".csv";
		return fileName;
	}

	default void writePerformanceDataToFile(LogHandler logHandler, String functionName, Optional<String> restFile)
			throws SeMoDeException {

		Map<String, WritableEvent> restMap = new HashMap<>();

		// if a benchmarking file is selected
		if (restFile.isPresent()) {
			BenchmarkingRESTAnalyzer restAnalyzer = new BenchmarkingRESTAnalyzer(Paths.get(restFile.get()));
			restMap = restAnalyzer.extractRESTEvents();
		}

		this.writePerformanceDataToFile(this.generateFileName(functionName), restMap, logHandler.getPerformanceData());
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
					} else {
						// there was an error due to api limitations or other problems
						// where the platform does not return a valid response with
						// the platform id included. The idea is to get these matching due
						// to the local start time and the relation to the start time of the
						// cloud function.
						Optional<WritableEvent> matchingEvent = this.findMatchingEvent(restEvent, performanceProviderMap);
						if (matchingEvent.isPresent()) {
							writer.write(matchingEvent.get().toCSVString());
						}
					}
					writer.write(System.lineSeparator());
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Writing to file failed");
		}
	}

	default Optional<WritableEvent> findMatchingEvent(WritableEvent localRestEvent, Map<String, WritableEvent> performanceProviderMap) {
		long localRestTime = localRestEvent.getStartTime().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
		for(String key : performanceProviderMap.keySet() ) {
			// one second is big enough to get the right matching and small enough to avoid false matchings.
			if ( Math.abs( localRestTime - performanceProviderMap.get(key).getStartTime().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli() ) < NETWORK_AND_PLATFORM_DELAY){
				return Optional.of(performanceProviderMap.get(key));
			}
		}
		
		return Optional.empty();
	}
}
