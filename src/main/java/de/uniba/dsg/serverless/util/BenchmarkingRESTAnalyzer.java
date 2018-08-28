package de.uniba.dsg.serverless.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.LocalRESTEvent;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;

public class BenchmarkingRESTAnalyzer {

	private static final Logger logger = LogManager.getLogger(BenchmarkingRESTAnalyzer.class.getName());

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(System.getProperty("DATE_TIME_FORMAT"));

	private final Path benchmarkingFile;

	public BenchmarkingRESTAnalyzer(Path benchmarkingFile) {
		this.benchmarkingFile = benchmarkingFile;
	}

	/**
	 * Extract the REST events from the local execution of the benchmarking utility
	 * and returns a map, where the key is the platformId and the value is a
	 * comprehensive local REST event.
	 * 
	 * @see {@link LocalRESTEvent}
	 * @return
	 * @throws SeMoDeException
	 */
	public Map<String, WritableEvent> extractRESTEvents() throws SeMoDeException {
		
		Map<String, LocalRESTEvent> extractedEvents = new HashMap<>();

		// read the lines
		// Sample line: 2018-05-25 13:44:54.826;[pool-2-thread-1];INFO ;de.uniba.dsg.serverless.benchmark.FunctionTrigger;69cfe08c-2b1e-4344-8a2c-0e7732f23146;284d7ba0-b0ce-40cb-9c5b-49696af5ded8;PLATFORMID
		try {
			List<String> lines = Files.readAllLines(benchmarkingFile);
			Predicate<String> isNotEmpty = s -> !s.trim().isEmpty();
			Function<String, String[]> splitLine = s -> s.split(System.getProperty("CSV_SEPARATOR"));
			Consumer<String[]> insertEvent = (String[] s) -> {
				// minimum 6, otherwise an error message is logged and can not be computed in this way
				if(s.length >= 6) {
					String uuid = s[5].trim();
					if (extractedEvents.containsKey(uuid) == false) {
						LocalRESTEvent event = new LocalRESTEvent();
						extractedEvents.put(uuid, event);
					}
	
					// parse other parameters
					LocalRESTEvent event = extractedEvents.get(uuid);
					String key = s[4];
					if ("START".equals(key)) {
						event.setStartTime(LocalDateTime.parse(s[0], formatter));
					} else if ("END".equals(key)) {
						event.setEndTime(LocalDateTime.parse(s[0], formatter));
					} else if ("ERROR".equals(key)) {
						event.setErroneous(true);
					} else if ("PLATFORMID".equals(key)){
						event.setPlatformId(s[6]);
					} else if ("CONTAINERID".equals(key)) {
						event.setContainerId(s[6]);
					} else {
						logger.warn("The following key is no REST event property: " + key);
					}
				} else {
					logger.warn("Check the benchmarking log file " + this.benchmarkingFile.toString() + " - corrupted line: " + s);
				}
			};

			lines.stream()
				.filter(isNotEmpty)
				.map(splitLine)
				.forEach(insertEvent);

		} catch (IOException e) {
			throw new SeMoDeException("Error while reading the file " + benchmarkingFile.toString(), e);
		}
		
		Map<String, WritableEvent> result = new HashMap<>();
		result.putAll(extractedEvents);
		return result;
	}
}
