package de.uniba.dsg.serverless.cli.performance;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;
import de.uniba.dsg.serverless.provider.LogHandler;
import de.uniba.dsg.serverless.provider.azure.AzureLogHandler;
import de.uniba.dsg.serverless.util.BenchmarkingRESTAnalyzer;

public interface PerformanceData {

	default String generateFileName(String functionName) {
		String dateText = new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date());
		String fileName = functionName + "-" + dateText + ".csv";
		return fileName;
	}
	
	default void writePerformanceDataToFile(LogHandler logHandler, String functionName, Optional<String> restFile) throws SeMoDeException {
		
		List<Map<String, WritableEvent>> elementList = new ArrayList<>();
		
		elementList.add(logHandler.getPerformanceData());

		// if a benchmarking file is selected
		if (restFile.isPresent()) {
			BenchmarkingRESTAnalyzer restAnalyzer = new BenchmarkingRESTAnalyzer(Paths.get(restFile.get()));
			elementList.add(restAnalyzer.extractRESTEvents());
		}

		logHandler.writePerformanceDataToFile(this.generateFileName(functionName), elementList);
	}
}
