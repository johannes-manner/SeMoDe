package de.uniba.dsg.serverless.pipeline.benchmark.util;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.pipeline.benchmark.model.LocalRESTEvent;
import de.uniba.dsg.serverless.pipeline.benchmark.model.WritableEvent;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BenchmarkingRESTAnalyzer {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private final Path benchmarkingFile;
    private final String platform;

    public BenchmarkingRESTAnalyzer(final String platform, final Path benchmarkingFile) {
        this.platform = platform;
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
        // Sample line: [;2020-03-25 13:05:15:121;] [INFO   ] ;aws;START;acb4addc-c649-498c-ae58-114a0e027e35
        try {
            final List<String> lines = Files.readAllLines(this.benchmarkingFile);
            final Predicate<String> isNotEmpty = s -> !s.trim().isEmpty();
            final Function<String, String[]> splitLine = s -> s.split(System.getProperty("CSV_SEPARATOR"));
            final Predicate<String[]> correctPlatform = s -> s[3].equals(this.platform);
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
                        event.setStartTime(LocalDateTime.parse(s[1]));
                    } else if ("END".equals(key)) {
                        event.setEndTime(LocalDateTime.parse(s[1]));
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

            lines.stream().filter(isNotEmpty).map(String::trim).map(splitLine).filter(correctPlatform).forEach(insertEvent);

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
