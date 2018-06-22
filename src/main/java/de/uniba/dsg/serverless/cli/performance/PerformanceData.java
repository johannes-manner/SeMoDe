package de.uniba.dsg.serverless.cli.performance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;
import de.uniba.dsg.serverless.provider.LogHandler;
import de.uniba.dsg.serverless.util.BenchmarkingRESTAnalyzer;

public interface PerformanceData {

	default String generateFileName(String functionName) {
		String dateText = new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date());
		String fileName = functionName + "-" + dateText + ".csv";
		return fileName;
	}
	
	default void writePerformanceDataToFile(LogHandler logHandler, String functionName, Optional<String> restFile) throws SeMoDeException {
		
		List<Map<String, WritableEvent>> elementList = new ArrayList<>();
		
		// if a benchmarking file is selected
		if (restFile.isPresent()) {
			BenchmarkingRESTAnalyzer restAnalyzer = new BenchmarkingRESTAnalyzer(Paths.get(restFile.get()));
			elementList.add(restAnalyzer.extractRESTEvents());
		}
		
		elementList.add(logHandler.getPerformanceData());		

		this.writePerformanceDataToFile(this.generateFileName(functionName), elementList);
	}
	
	default void writePerformanceDataToFile(String fileName, List<Map<String, WritableEvent>> maps) throws SeMoDeException {
		try {
			String OUTPUT_DIRECTORY = "performanceData";

			if (!Files.exists(Paths.get(OUTPUT_DIRECTORY))) {
				Files.createDirectory(Paths.get(OUTPUT_DIRECTORY));
			}
			Path file = Files.createFile(Paths.get(OUTPUT_DIRECTORY + "/" + fileName));
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				
				// Writing header line
				for(Map<String, WritableEvent> map : maps) {
					if(map.isEmpty()) {
						throw new SeMoDeException("The platform map is empty. The most likely reason is the wrong start and end time.");
					} else {
						String key = map.keySet().iterator().next();
						writer.write(map.get(key).getCSVMetadata());
					}
				}
				writer.write(System.lineSeparator());
				
				// Writing rows
				Map<String, WritableEvent> keyMap = maps.get(0);
				for(String key : keyMap.keySet()) {
					for(Map<String, WritableEvent> map : maps) {
						// if the first map contains more elements, than the others
						// a "left outer join" is made here
						WritableEvent event = map.get(key);
						if(event != null) {
							writer.write(event.toCSVString());
						}
					}
					writer.write(System.lineSeparator());
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Writing to file failed");
		}
	}
}
