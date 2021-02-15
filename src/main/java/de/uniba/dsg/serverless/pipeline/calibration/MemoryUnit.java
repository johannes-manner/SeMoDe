package de.uniba.dsg.serverless.pipeline.calibration;

public enum MemoryUnit {
    B, KB, MB, GB;

    public long toBytes(long memory) {
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

    public long fromBytes(long memory) {
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