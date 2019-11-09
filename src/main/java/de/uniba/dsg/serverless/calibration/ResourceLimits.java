package de.uniba.dsg.serverless.calibration;

import com.google.gson.Gson;
import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLimits {
    public final double cpuLimit;
    public final long memoryLimit;
    public final boolean pinCPU;

    public static final Path FOLDER = Paths.get("profiling", "src", "main", "resources");

    /**
     * Creates resource limits using MB as the unit.
     *
     * @param cpuLimit    cpu Quota
     * @param memoryLimit memory limit
     */
    public ResourceLimits(double cpuLimit, boolean pinCPU, long memoryLimit) {
        this(cpuLimit, pinCPU, memoryLimit, MemoryUnit.MB);
    }

    /**
     * Creates resource limits.
     *
     * @param cpuLimit    cpu quota
     * @param memoryLimit memory limit
     * @param unit        memory unit
     */
    public ResourceLimits(double cpuLimit, boolean pinCPU, long memoryLimit, MemoryUnit unit) {
        this.pinCPU = pinCPU;
        this.cpuLimit = cpuLimit;
        this.memoryLimit = unit.toBytes(memoryLimit);
    }

    public static ResourceLimits unlimited() {
        return new ResourceLimits(0.0, false, 0L);
    }

    public static ResourceLimits fromFile(String fileName) throws SeMoDeException {
        Gson parser = new Gson();
        try {
            Reader reader = new BufferedReader(new FileReader(FOLDER.resolve(fileName).toString()));
            return parser.fromJson(reader, ResourceLimits.class);
        } catch (IOException e) {
            throw new SeMoDeException("Resource limits could not be read. ", e);
        }
    }

    public long getMemoryLimitInMb() {
        return MemoryUnit.MB.fromBytes(this.memoryLimit);
    }

    @Override
    public String toString() {
        return "Resource Limits (quota=" + this.cpuLimit + ", memory=" + MemoryUnit.MB.fromBytes(this.memoryLimit) + "MB)";
    }
}


enum MemoryUnit {
    B, KB, MB, GB;

    long toBytes(long memory) {
        switch (this) {
            case GB:
                memory *= 1024;
            case MB:
                memory *= 1024;
            case KB:
                memory *= 1024;
            default:
                return memory;
        }
    }

    long fromBytes(long memory) {
        switch (this) {
            case GB:
                memory /= 1024;
            case MB:
                memory /= 1024;
            case KB:
                memory /= 1024;
            default:
                return memory;
        }
    }
}
