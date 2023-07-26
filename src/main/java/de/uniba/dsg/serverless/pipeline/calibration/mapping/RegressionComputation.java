package de.uniba.dsg.serverless.pipeline.calibration.mapping;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;
import java.util.Map;

@Slf4j
public final class RegressionComputation {


    /**
     * This method creates a simple function based on a simple regression (commons math3).
     * Look at docu there for getting the details of the regression.
     *
     * @param metrics key, value pair: key is cpu quota or memory setting, value is the corresponding list of execution metrics
     *                (currently the unit are gflops)
     * @return
     */
    public static SimpleFunction computeRegression(Map<Double, List<Double>> metrics) {
        SimpleRegression regression = new SimpleRegression();
        log.info("Compute regression ...");
        for (Double key : metrics.keySet()) {
            log.debug("Add quota/memory size " + key + " value " + metrics.get(key) + " to linear regression.");
            for (Double item : metrics.get(key)) {
                regression.addData(key, item);
            }
        }
        log.info("Pearson r: " + regression.getR() + " - r²: " + regression.getRSquare());
        return new SimpleFunction(regression.getSlope(), regression.getIntercept(), regression.getR(), regression.getRSquare());

    }
}
