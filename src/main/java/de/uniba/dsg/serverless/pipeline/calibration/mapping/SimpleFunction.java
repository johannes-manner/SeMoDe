package de.uniba.dsg.serverless.pipeline.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.calibration.util.PhysicalCoreFinder;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

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
    public void computeMemoryForCpuShares(final SimpleFunction function) {
        try {
            for (int i = 1; i <= PhysicalCoreFinder.getPhysicalCores(); i++) {
                log.info("cpu " + i + " comparable to " + ((i * this.slope + this.intercept - function.intercept) / function.slope) + " MB");
            }
        } catch (SeMoDeException e) {
            log.warn("An error occurred during the determination of the physical cores, but that's not system critical!");
        }
    }

    @Override
    public String toString() {
        return "r: " + this.r + " - R²: " + this.rSquare + " f(x)" + " = " + this.slope + " * x " + " + " + this.intercept;
    }
}
