package de.uniba.dsg.serverless.azure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniba.dsg.serverless.model.PerformanceData;

public class AzureLogHandler {

	private static final String OUTPUT_DIRECTORY = "performanceData";

	private static final Logger logger = Logger.getLogger(AzureLogHandler.class.getName());
	
	private final String apiURL;

	private final String apiKey;
	private final String functionName;

	public AzureLogHandler(String applicationID, String apiKey, String functionName) {
		this.apiKey = apiKey;
		this.functionName = functionName;
		
		this.apiURL = "https://api.applicationinsights.io/v1/apps/" + applicationID + "/query?query=requests%20%7C%20order%20by%20timestamp%20asc";
	}

	/**
	 * Retrieves the performance data from the specified cloud storage and saves it
	 * to the specified file
	 * 
	 * @param fileName
	 *            name of the output file
	 */
	public void writePerformanceDataToFile(String fileName) {
		try {
			List<PerformanceData> performanceDataList = getPerformanceData();
			
			if (!Files.exists(Paths.get(OUTPUT_DIRECTORY))) {
				Files.createDirectory(Paths.get(OUTPUT_DIRECTORY));
			}

			Path file = Files.createFile(Paths.get(OUTPUT_DIRECTORY + "/" + fileName));
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				writer.write(PerformanceData.getCSVMetadata() + System.lineSeparator());
				for (PerformanceData performanceData : performanceDataList) {
					writer.write(performanceData.toCSVString() + System.lineSeparator());
				}
			}

		} /*catch (SeMoDeException e) {
			logger.severe(e.getMessage());
			logger.severe("Data handler is terminated due to an error.");
		}*/ catch (IOException e) {
			logger.severe("Writing to CSV file failed.");
		}
	}
	
	private List<PerformanceData> getPerformanceData() {
		List<PerformanceData> performanceDataList = new ArrayList<>();
		
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		
		String json = getRequests();
		try {
			JsonNode tablesNode = mapper.readTree(json).get("tables");
			JsonNode rowsNode = tablesNode.get(0).get("rows");
			
			Iterator<JsonNode> it = rowsNode.iterator();
			while(it.hasNext()) {
				JsonNode rowNode = it.next();
				String time = rowNode.get(0).asText();
				LocalDateTime startTime = AzureLogAnalyzer.parseTime(time);
				String id = rowNode.get(1).asText();
				String functionName = rowNode.get(3).asText();
				double duration = rowNode.get(7).asDouble();
				String container = rowNode.get(30).asText();
				
				if (functionName.equals(this.functionName)) {
					PerformanceData data = new PerformanceData(functionName, container, id, startTime, duration, -1, -1, -1);
					performanceDataList.add(data);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return performanceDataList;
	}
	
	private String getRequests() {
		String requests = "";
		
		try {
			URL url = new URL(apiURL);
			
		    HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
		    con.setDoInput(true);
		    con.setRequestMethod("GET");
		    con.setRequestProperty("Content-Type", "application/json");
		    con.setRequestProperty("x-api-key", apiKey);
	
	        BufferedReader in;
				in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	requests = requests.concat(inputLine);
	        }
	        in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return requests;
	}

}
