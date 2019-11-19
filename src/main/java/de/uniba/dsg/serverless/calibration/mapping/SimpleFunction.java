package de.uniba.dsg.serverless.calibration.mapping;

public class SimpleFunction {

    private final double slope;
    private final double intercept;

    public SimpleFunction(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    /**
     * Compute the result of the regression function for specific doubel values.
     *
     * @param x - input parameter for f(x) = m * x + t
     * @return computed value
     */
    public double computeFunctionResult(double x){
        return this.slope * x + this.intercept;
    }

    /**
     * Assumed that this(x) and function(y) are equal, the following method computes the value for x.
     *
     *
     * @param function
     * @param y input for function
     * @return
     */
    public double computeDependentResult(SimpleFunction function, double y){
        return (function.slope * y + function.intercept - this.intercept) / this.slope;
    }

    @Override
    public String toString() {
        return "f(x)" + " = " + this.slope + " * x " + " + " + this.intercept;
    }
}
