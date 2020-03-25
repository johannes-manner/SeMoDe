package de.uniba.dsg.serverless.util;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.model.LocalRESTEvent;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.model.WritableEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BenchmarkingRESTAnalyzer {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern(System.getProperty("DATE_TIME_FORMAT"));

    private final Path benchmarkingFile;

    public BenchmarkingRESTAnalyzer(final Path benchmarkingFile) {
        this.benchmarkingFile = benchmarkingFile;
    }

    /**
     * Extract the REST events from the local execution of the benchmarking utility
     * and returns a map, where the key is the platformId and the value is a
     * comprehensive local REST event. <br/>
     * <p>
     * The local uuid is used to get consistent events and the platform id is used
     * as the key of the returned map to match the platform values.
     *
     * @return
     * @throws SeMoDeException
     * @see {@link LocalRESTEvent}
     */
    public Map<String, WritableEvent> extractRESTEvents() throws SeMoDeException {

        final Map<String, LocalRESTEvent> extractedEvents = new HashMap<>();

        // read the lines
        // Sample line: 2018-05-25 13:44:54.826;[pool-2-thread-1];INFO
        // ;de.uniba.dsg.serverless.benchmark.FunctionTrigger;69cfe08c-2b1e-4344-8a2c-0e7732f23146;284d7ba0-b0ce-40cb-9c5b-49696af5ded8;PLATFORMID
        try {
            final List<String> lines = Files.readAllLines(this.benchmarkingFile);
            final Predicate<String> isNotEmpty = s -> !s.trim().isEmpty();
            final Function<String, String[]> splitLine = s -> s.split(System.getProperty("CSV_SEPARATOR"));
            final Consumer<String[]> insertEvent = (String[] s) -> {
                // minimum 6, otherwise an error message is logged and can not be computed in
                // this way
                if (s.length >= 6) {
                    final String uuid = s[5].trim();
                    if (extractedEvents.containsKey(uuid) == false) {
                        final LocalRESTEvent event = new LocalRESTEvent();
                        extractedEvents.put(uuid, event);
                    }

                    // parse other parameters
                    final LocalRESTEvent event = extractedEvents.get(uuid);
                    final String key = s[4];
                    if ("START".equals(key)) {
                        event.setStartTime(LocalDateTime.parse(s[0], formatter));
                    } else if ("END".equals(key)) {
                        event.setEndTime(LocalDateTime.parse(s[0], formatter));
                    } else if ("ERROR".equals(key)) {
                        event.setErroneous(true);
                    } else if ("PLATFORMID".equals(key)) {
                        event.setPlatformId(s[6]);
                    } else if ("CONTAINERID".equals(key)) {
                        event.setContainerId(s[6]);
                    } else if ("VMIDENTIFICATION".equals(key)) {
                        event.setVmIdentification(s[6]);
                    } else if ("CPUMODEL".equals(key)) {
                        event.setCpuModel(s[6]);
                    } else if ("CPUMODELNAME".equals(key)) {
                        event.setCpuModelName(s[6]);
                    } else {
                        logger.warning("The following key is no REST event property: " + key);
                    }
                } else {
                    logger.warning("Check the benchmarking log file " + this.benchmarkingFile.toString()
                            + " - corrupted line: " + s);
                }
            };

            lines.stream().filter(isNotEmpty).map(splitLine).forEach(insertEvent);

        } catch (final IOException e) {
            throw new SeMoDeException("Error while reading the file " + this.benchmarkingFile.toString(), e);
        }

        final Map<String, WritableEvent> result = new HashMap<>();
        for (final String uuid : extractedEvents.keySet()) {
            final WritableEvent value = extractedEvents.get(uuid);
            result.put(value.getPlatformId(), value);
        }
        return result;
    }
}
