package de.uniba.dsg.serverless.pipeline.calibration.mapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleFunction {

    private final double slope;
    private final double intercept;

    public SimpleFunction(final double slope, final double intercept) {
        this.slope = slope;
        this.intercept = intercept;
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
        log.info("Compute the memory Setting (x-axis value) which represents one core on the machine - important for multithreaded apps.");
        log.info("cpuShare = 1 => memorySetting: " + ((this.slope - function.intercept + this.intercept) / function.slope));
        return (function.slope * y + function.intercept - this.intercept) / this.slope;
    }

    @Override
    public String toString() {
        return "f(x)" + " = " + this.slope + " * x " + " + " + this.intercept;
    }
}
