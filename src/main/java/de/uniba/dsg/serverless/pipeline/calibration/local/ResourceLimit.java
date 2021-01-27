package de.uniba.dsg.serverless.pipeline.calibration.local;

import de.uniba.dsg.serverless.pipeline.calibration.MemoryUnit;

public class ResourceLimit {
    public final double cpuLimit;
    public final long memoryLimit;
    public final boolean pinCPU;

    /**
     * Creates resource limits using MB as the unit.
     *
     * @param cpuLimit    cpu Quota
     * @param memoryLimit memory limit
     */
    public ResourceLimit(double cpuLimit, boolean pinCPU, long memoryLimit) {
        this(cpuLimit, pinCPU, memoryLimit, MemoryUnit.MB);
    }

    /**
     * Creates resource limits.
     *
     * @param cpuLimit    cpu quota
     * @param memoryLimit memory limit
     * @param unit        memory unit
     */
    public ResourceLimit(double cpuLimit, boolean pinCPU, long memoryLimit, MemoryUnit unit) {
        this.pinCPU = pinCPU;
        this.cpuLimit = cpuLimit;
        this.memoryLimit = unit.toBytes(memoryLimit);
    }

    public static ResourceLimit unlimited() {
        return new ResourceLimit(0.0, false, 0L);
    }

    public long getMemoryLimitInMb() {
        return MemoryUnit.MB.fromBytes(this.memoryLimit);
    }

    @Override
    public String toString() {
        return "Resource Limits (quota=" + this.cpuLimit + ", memory=" + MemoryUnit.MB.fromBytes(this.memoryLimit) + "MB)";
    }
}

