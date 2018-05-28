package de.uniba.dsg.serverless.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;

public interface LogHandler {

	public Map<String, WritableEvent> getPerformanceData() throws SeMoDeException;

	public default void writePerformanceDataToFile(String fileName, List<Map<String, WritableEvent>> maps) throws SeMoDeException {
		try {
			String OUTPUT_DIRECTORY = "performanceData";

			if (!Files.exists(Paths.get(OUTPUT_DIRECTORY))) {
				Files.createDirectory(Paths.get(OUTPUT_DIRECTORY));
			}
			Path file = Files.createFile(Paths.get(OUTPUT_DIRECTORY + "/" + fileName));
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				
				// Writing header line
				for(Map<String, WritableEvent> map : maps) {
					writer.write(map.get(map.keySet().iterator().next()).getCSVMetadata());
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
