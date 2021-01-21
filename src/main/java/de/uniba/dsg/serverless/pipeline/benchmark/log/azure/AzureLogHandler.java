package de.uniba.dsg.serverless.pipeline.benchmark.log.azure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.CharStreams;
import com.google.common.net.UrlEscapers;
import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.pipeline.benchmark.log.LogHandler;
import de.uniba.dsg.serverless.pipeline.benchmark.model.PerformanceData;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

@Deprecated(since = "January 2021")
public class AzureLogHandler implements LogHandler {

    private static final FileLogger logger = ArgumentProcessor.logger;
    private static final DateTimeFormatter QUERY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final String apiURL;
    private final String apiKey;
    private final String functionName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public AzureLogHandler(final String applicationID, final String apiKey, final String functionName, final LocalDateTime startTime,
                           final LocalDateTime endTime) {
        this.apiKey = apiKey;
        this.functionName = functionName;
        this.startTime = startTime;
        this.endTime = endTime;

        this.apiURL = "https://api.applicationinsights.io/v1/apps/" + applicationID + "/query";
    }

    /**
     * Retrieves the performance data from Application Insights via its REST API.
     *
     * @return The list of performance data.
     * @throws SeMoDeException If there was an exception while retrieving or parsing the performance data.
     */
    @Override
    public List<PerformanceData> getPerformanceData() throws SeMoDeException {
//        final Map<String, WritableEvent> performanceData = new HashMap<>();
//
//        final String functionRequests = this.getRequestsAsJSON();
//
//        final JsonFactory factory = new JsonFactory();
//        final ObjectMapper mapper = new ObjectMapper(factory);
//
//        try {
//            final JsonNode tableNode = mapper.readTree(functionRequests).get("tables").get(0);
//            final JsonNode columnsNode = tableNode.get("columns");
//            final JsonNode rowsNode = tableNode.get("rows");
//
//            final Map<String, Integer> columnsIndex = this.parseColumns(columnsNode);
//
//            for (final JsonNode rowNode : rowsNode) {
//                // Function execution data
//                final String functionName = rowNode.get(columnsIndex.get("r_name")).asText();
//                final String container = rowNode.get(columnsIndex.get("r_container")).asText();
//                final String id = rowNode.get(columnsIndex.get("r_id")).asText();
//                final String start = rowNode.get(columnsIndex.get("r_timestamp")).asText();
//                final String customJson = rowNode.get(columnsIndex.get("r_custom")).asText();
//                final String end = mapper.readTree(customJson).get("EndTime").asText();
//                final double duration = rowNode.get(columnsIndex.get("r_duration")).asDouble();
//
//                LocalDateTime startTime = AzureLogAnalyzer.parseTime(start);
//                LocalDateTime endTime = AzureLogAnalyzer.parseTime(end);
//
//                // Azure SDK calls do not use the local time offset
//                // Therefore an adaption is necessary here, because the standard used by azure is UTC 0
//                final Instant instant = Instant.now(); //can be LocalDateTime
//                final ZoneId systemZone = ZoneId.systemDefault(); // my timezone
//                final ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(instant);
//                final int hours = currentOffsetForMyZone.getTotalSeconds() / 3600;
//                startTime = startTime.plusHours(hours);
//                endTime = endTime.plusHours(hours);
//
//                // Host startup data
//                final String message = rowNode.get(columnsIndex.get("t_message")).asText();
//
//                double hostStartupDuration = -1;
//                // If host was not started for the request, left join is empty and message is empty string
//                if (!message.equals("")) {
//                    hostStartupDuration = AzureLogAnalyzer.parseHostStartupDuration(message);
//                }
//
//                final PerformanceData data = new PerformanceData(functionName, container, id, startTime, endTime,
//                        hostStartupDuration, duration, -1, -1, -1);
//                performanceData.put(id, data);
//            }
//        } catch (final IOException e) {
//            throw new SeMoDeException("Exception while parsing requests via REST API from Application Insights", e);
//        }
//
//        return performanceData;
        return List.of();
    }

    /**
     * Calls the REST API of Application Insights to retrieve the function requests and returns the response as
     * JSON-formatted string.
     *
     * @return Response of the REST API as a JSON-formatted string.
     * @throws SeMoDeException If calling the REST API failed.
     */
    private String getRequestsAsJSON() throws SeMoDeException {
        final String tracesJoin = "traces "
                + "| project t_timestamp=timestamp, t_message=message, t_container=cloud_RoleInstance, t_itemId = substring(itemId, 9) "
                + "| where t_timestamp > todatetime('" + this.startTime.format(QUERY_DATE_FORMATTER) + "') "
                + "and t_timestamp < todatetime('" + this.endTime.format(QUERY_DATE_FORMATTER) + "') "
                + "and t_message startswith 'Host started'";

        final String query = "requests "
                + "| project r_timestamp=timestamp, r_id=id, r_name=name, r_duration=duration, r_custom=customDimensions, r_container=cloud_RoleInstance, r_itemId = substring(itemId,9) "
                + "| where r_timestamp > todatetime('" + this.startTime.format(QUERY_DATE_FORMATTER) + "') "
                + "and r_timestamp < todatetime('" + this.endTime.format(QUERY_DATE_FORMATTER) + "') "
                + "and r_name == '" + this.functionName + "' "
                + "| join kind=leftouter (" + tracesJoin + ") on $left.r_itemId == $right.t_itemId "
                + "| order by r_timestamp asc";

        return this.runQuery(query);
    }

    /**
     * Returns the url of the REST API of Application Insights for a given query.
     *
     * @param query The query to declare what to fetch from Application Insights.
     * @return The url of the REST API
     */
    private String getApiUrlForQuery(final String query) {
        final String escapedQuery = UrlEscapers.urlPathSegmentEscaper().escape(query);
        return this.apiURL + "?query=" + escapedQuery;
    }

    /**
     * Runs a query via the REST API of Application Insights and returns the response as string.
     *
     * @param query The query to declare what to fetch from Application Insights.
     * @return The response as string.
     * @throws SeMoDeException If calling the REST API failed.
     */
    private String runQuery(final String query) throws SeMoDeException {
        try {
            final URL url = new URL(this.getApiUrlForQuery(query));

            final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(false);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("x-api-key", this.apiKey);

            try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                return CharStreams.toString(in);
            }
        } catch (final IOException e) {
            throw new SeMoDeException("Exception while receiving requests via REST API from Application Insights", e);
        }
    }

    /**
     * Parses the columns of the JSON-formatted answer to later reference items by its name instead of its position in
     * the array.
     *
     * @param columnsNode The columns node of the server response.
     * @return A map assigning each column the index in the array.
     */
    private Map<String, Integer> parseColumns(final JsonNode columnsNode) {
        final Map<String, Integer> map = new HashMap<>();

        int index = 0;
        for (final JsonNode columnNode : columnsNode) {
            final String name = columnNode.get("name").asText();
            map.put(name, index);
            index++;
        }

        return map;
    }
}
