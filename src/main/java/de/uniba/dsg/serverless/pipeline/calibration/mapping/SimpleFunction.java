package de.uniba.dsg.serverless.pipeline.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.calibration.util.PhysicalCoreFinder;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SimpleFunction {

    private final double slope;
    private final double intercept;
    private final double r;
    private final double rSquare;

    public SimpleFunction(final double slope, final double intercept, final double r, final double rSquare) {
        this.slope = slope;
        this.intercept = intercept;
        this.r = r;
        this.rSquare = rSquare;
    }

    /**
     * Compute the result of the regression function for specific doubel values.
     *
     * @param x - input parameter for f(x) = m * x + t
     * @return computed value
     */
    public double computeFunctionResult(final double x) {
        return this.slope * x + this.intercept;
    }

    /**
     * Assumed that this(x) and function(y) are equal, the following method computes the value for x.
     *
     * @param function
     * @param y        input for function
     * @return
     */
    public double computeDependentResult(final SimpleFunction function, final double y) {
        log.info("cpuShare = f(memorySize)= (" + function.slope + "* y + " + function.intercept + " - " + this.intercept + ") / " + this.slope);
        return (function.slope * y + function.intercept - this.intercept) / this.slope;
    }

    /**
     * y = m*x +t
     * computes x
     *
     * @param y
     * @return
     */
    public double computeX(final double y) {
        return (y - this.intercept) / this.slope;
    }

    /**
     * Gets the number of cpus from the calibration and logs the equivalent settings for the memory.
     * <p>
     * E.g.
     * cpu 1 comparable to 1135 MB
     * cpu 2 comparable to 2305 MB ...
     * <p>
     * This output is only valid, when executed on the machine, where also the calibration was executed. Otherwise the info is somehow corrupted,
     * since the number of cores might be incorrect.
     *
     * @param function
     */
    public void computeCPUMemoryEquivalents(final SimpleFunction function) {
        try {
            for (int i = 1; i <= PhysicalCoreFinder.getPhysicalCores(); i++) {
                log.info("cpu " + i + " comparable to " + ((i * this.slope + this.intercept - function.intercept) / function.slope) + " MB");
            }
        } catch (SeMoDeException e) {
            log.warn("An error occurred during the determination of the physical cores, but that's not system critical!");
        }
    }

    /**
     * Duplicated version of {@link #computeCPUMemoryEquivalents(SimpleFunction)}.
     * Could be consolidated in future when a bigger refactoring is necessary to handle multiple profile configurations
     * for several machines. <br/>
     * <p>
     * Return a map of cpu equivalents, {[1,1135],[2,2505]..}.
     *
     * @param noOfCPUs
     * @param function
     * @return
     */
    public Map<Integer, Integer> computeCPUMemoryEquivalents(int noOfCPUs, final SimpleFunction function) {
        Map<Integer, Integer> cpuMemoryEquivalents = new HashMap<>();
        for (int i = 1; i <= noOfCPUs; i++) {
            int cpuInMB = (int) ((i * this.slope + this.intercept - function.intercept) / function.slope);
            cpuMemoryEquivalents.put(i, cpuInMB);
            log.info("cpu " + i + " comparable to " + cpuInMB + " MB");
        }
        return cpuMemoryEquivalents;
    }

    @Override
    public String toString() {
        return String.format("r: %4f - R²:  %4f f(x) = %4f * x + %4f", this.r, this.rSquare, this.slope, this.intercept);
    }
}
