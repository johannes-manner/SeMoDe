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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.google.common.net.UrlEscapers;

import de.uniba.dsg.serverless.model.PerformanceData;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class AzureLogHandler {

	private static final Logger logger = Logger.getLogger(AzureLogHandler.class.getName());

	private static final String OUTPUT_DIRECTORY = "performanceData";

	private static final String MESSAGE_HOST_STARTED = "Host started";

	private final String apiURL;
	private final String apiKey;
	private final String functionName;

	public AzureLogHandler(String applicationID, String apiKey, String functionName) {
		this.apiKey = apiKey;
		this.functionName = functionName;

		this.apiURL = "https://api.applicationinsights.io/v1/apps/" + applicationID + "/query";
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

		} catch (SeMoDeException e) {
			logger.severe(e.getMessage());
			logger.severe("Data handler is terminated due to an error.");
		} catch (IOException e) {
			logger.severe("Writing to CSV file failed.");
		}
	}

	private List<PerformanceData> getPerformanceData() throws SeMoDeException {
		List<PerformanceData> performanceData = new ArrayList<>();

		Set<String> startedContainers = new HashSet<>();
		Map<String, Double> hostStartupDurations = getHostStartupDurations();

		String functionRequests = getRequestsAsJSON();

		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);

		try {
			JsonNode tableNode = mapper.readTree(functionRequests).get("tables").get(0);
			JsonNode columnsNode = tableNode.get("columns");
			JsonNode rowsNode = tableNode.get("rows");

			Map<String, Integer> columnsIndex = parseColumns(columnsNode);

			for (JsonNode rowNode : rowsNode) {
				String start = rowNode.get(columnsIndex.get("timestamp")).asText();
				String id = rowNode.get(columnsIndex.get("id")).asText();
				String functionName = rowNode.get(columnsIndex.get("name")).asText();
				double duration = rowNode.get(columnsIndex.get("duration")).asDouble();
				String dimJson = rowNode.get(columnsIndex.get("customDimensions")).asText();
				String container = rowNode.get(columnsIndex.get("cloud_RoleInstance")).asText();

				String end = mapper.readTree(dimJson).get("EndTime").asText();

				LocalDateTime startTime = AzureLogAnalyzer.parseTime(start);
				LocalDateTime endTime = AzureLogAnalyzer.parseTime(end);

				if (functionName.equals(this.functionName)) {
					// TODO: Find a better solution to add the host startup time to a function
					double startupDuration = 0;
					if (!startedContainers.contains(container)) {
						startupDuration = hostStartupDurations.get(container);
						startedContainers.add(container);
					}

					PerformanceData data = new PerformanceData(functionName, container, id, startTime, endTime,
							startupDuration, duration, -1, -1, -1);
					performanceData.add(data);
				}
			}

		} catch (IOException e) {
			throw new SeMoDeException("Exception while parsing requests via REST API from Application Insights", e);
		}

		return performanceData;
	}

	private Map<String, Double> getHostStartupDurations() throws SeMoDeException {
		Map<String, Double> hostStartupDurations = new HashMap<>();

		String functionTraces = getTracesAsJSON();

		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);

		try {
			JsonNode tableNode = mapper.readTree(functionTraces).get("tables").get(0);
			JsonNode columnsNode = tableNode.get("columns");
			JsonNode rowsNode = tableNode.get("rows");

			Map<String, Integer> columnsIndex = parseColumns(columnsNode);

			for (JsonNode rowNode : rowsNode) {
				String message = rowNode.get(columnsIndex.get("message")).asText();

				if (message.startsWith(MESSAGE_HOST_STARTED)) {
					String container = rowNode.get(columnsIndex.get("cloud_RoleInstance")).asText();
					double hostStartupDuration = AzureLogAnalyzer.parseHostStartupDuration(message);

					hostStartupDurations.put(container, hostStartupDuration);
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Exception while parsing traces via REST API from Application Insights", e);
		}

		return hostStartupDurations;
	}

	private String getRequestsAsJSON() throws SeMoDeException {
		return runQuery("requests | order by timestamp asc");
	}

	private String getTracesAsJSON() throws SeMoDeException {
		return runQuery("traces | order by timestamp asc");
	}

	private String getApiUrlForQuery(String query) {
		String escapedQuery = UrlEscapers.urlPathSegmentEscaper().escape(query);
		return apiURL + "?query=" + escapedQuery;
	}

	private String runQuery(String query) throws SeMoDeException {
		try {
			URL url = new URL(getApiUrlForQuery(query));

			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(false);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("x-api-key", apiKey);

			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				return CharStreams.toString(in);
			}
		} catch (IOException e) {
			throw new SeMoDeException("Exception while receiving requests via REST API from Application Insights", e);
		}
	}

	private Map<String, Integer> parseColumns(JsonNode columnsNode) {
		Map<String, Integer> map = new HashMap<>();

		int index = 0;
		for (JsonNode columnNode : columnsNode) {
			String name = columnNode.get("name").asText();
			map.put(name, index);
			index++;
		}

		return map;
	}

}
