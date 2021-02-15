package de.uniba.dsg.serverless.pipeline.calibration.profiling;

import com.github.dockerjava.api.model.CpuUsageConfig;
import com.github.dockerjava.api.model.Statistics;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.IntStream;

public class ContainerMetrics {
    public static final String STATS_TIME = "stats_time";
    public static final String STATS_TOTAL_CPU_USAGE = "stats_total_cpu_usage";
    public static final String MEMORY_LIMIT = "memory_limit";
    public static final String MEMORY_CACHE = "memory_cache";
    public static final String MEMORY_USAGE = "memory_usage";
    public static final List<String> CUMULATIVE = Arrays.asList(STATS_TIME, STATS_TOTAL_CPU_USAGE);
    public List<String> relevantMetrics = new ArrayList<>();
    private final String timeStamp;
    private final Map<String, Long> metrics;

    private ContainerMetrics(final HashMap<String, Long> metrics, final String timeStamp) throws SeMoDeException {
        this.timeStamp = timeStamp;
        this.metrics = metrics;
        this.relevantMetrics.addAll(metrics.keySet());
    }

    /**
     * Creates a Metrics based on two metrics. Calculates a delta Metrics which (if the metric is in CUMULATIVE) subtracts to - from
     */
    public ContainerMetrics(final ContainerMetrics from, final ContainerMetrics to) {
        this.timeStamp = from.timeStamp;
        this.relevantMetrics = to.relevantMetrics;
        this.metrics = new HashMap<>();
        CUMULATIVE.forEach(s -> to.metrics.compute(s, (k, v) -> v - from.metrics.get(s)));
    }

    /**
     * Creates a Container Metrics based on a docker statistics read.
     *
     * @param stats              statistics read from docker API
     * @param containerStartTime start time of the container
     * @return ContainerMetrics
     * @throws SeMoDeException Invalid stats
     */
    public static ContainerMetrics fromStatistics(final Statistics stats, final long containerStartTime) throws SeMoDeException {
        validateStats(stats);
        final HashMap<String, Long> map = new HashMap<>();

        final long time = parseTime(stats.getRead());
        final long relativeTime = time - containerStartTime;
        if (relativeTime < 0) {
            throw new SeMoDeException("Illegal containerStartTime.");
        }
        map.put(STATS_TIME, relativeTime);

        final long totalCpu = Optional.ofNullable(stats.getCpuStats().getCpuUsage())
                .map(CpuUsageConfig::getTotalUsage)
                .orElse(-1L);
        map.put(STATS_TOTAL_CPU_USAGE, totalCpu);

        // per CPU stats
        final List<Long> usagePerCpu = Optional.ofNullable(stats.getCpuStats().getCpuUsage().getPercpuUsage()).orElse(new ArrayList<>());
        IntStream.range(0, usagePerCpu.size()).forEach(i -> {
            map.put("cpu_" + i, usagePerCpu.get(i));
        });
        map.put(MEMORY_USAGE, stats.getMemoryStats().getUsage());
        map.put(MEMORY_CACHE, stats.getMemoryStats().getStats().getCache());
        map.put(MEMORY_LIMIT, stats.getMemoryStats().getLimit());

        // Network not included for now

        return new ContainerMetrics(map, stats.getRead());
    }

    private static void validateStats(final Statistics stats) throws SeMoDeException {
        if (stats.getMemoryStats() == null
                || stats.getMemoryStats().getStats() == null
                || stats.getCpuStats() == null
                || stats.getCpuStats().getCpuUsage() == null) {
            throw new SeMoDeException("Stats must not be null.");
        }
    }

    /**
     * Parses the time as ISO_DATE_TIME
     *
     * @param time Time to be formatted as an ISO_DATE_TIME (e.g. 2019-01-01T10:10:30.1337Z)
     * @return time as long
     * @throws SeMoDeException when the time is not of ISO_DATE_TIME format
     */
    public static long parseTime(final String time) throws SeMoDeException {
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (final DateTimeParseException e) {
            throw new SeMoDeException("Date does not have the correct format. time:" + time);
        }
    }

    /**
     * Parses from and to and returns the difference between the two instances in milliseconds.
     *
     * @param from Start to formatted as an ISO_DATE_TIME (e.g. 2019-01-01T10:10:30.1337Z)
     * @param to   Start to formatted as an ISO_DATE_TIME
     * @return difference in milliseconds
     * @throws SeMoDeException when from or to are not formatted as ISO_DATE_TIME
     */
    public static long timeDifference(final String from, final String to) throws SeMoDeException {
        try {
            final LocalDateTime startTime = LocalDateTime.parse(from, DateTimeFormatter.ISO_DATE_TIME);
            final LocalDateTime currentTime = LocalDateTime.parse(to, DateTimeFormatter.ISO_DATE_TIME);
            return Duration.between(startTime, currentTime).toMillis();
        } catch (final DateTimeParseException e) {
            throw new SeMoDeException("Date does not have the correct format. from:" + from + " to:" + to, e);
        }
    }

    public Long getMetric(final String metric) throws SeMoDeException {
        return Optional.ofNullable(this.metrics.get(metric))
                .orElseThrow(() -> new SeMoDeException("Metrics not available: " + metric));
    }

    public boolean containsMetric(final String key) {
        return this.metrics.containsKey(key);
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public String formatCSVLine() {
        final List<String> out = new ArrayList<>();
        out.add(this.timeStamp);
        for (final String s : this.relevantMetrics) {
            out.add(this.metrics.get(s).toString());
        }
        return String.join(",", out);
    }

}
