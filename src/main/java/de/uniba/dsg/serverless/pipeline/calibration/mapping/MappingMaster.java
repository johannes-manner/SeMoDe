package de.uniba.dsg.serverless.pipeline.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MappingMaster {

    private SimpleFunction localRegressionFunction;
    private SimpleFunction providerRegressionFunction;

    public MappingMaster() {
    }

    // TODO
    private void computeFunctions(Map<Double, List<Double>> localMetricsMap, Map<Double, List<Double>> providerMetricsMap) {
        this.localRegressionFunction = RegressionComputation.computeRegression(localMetricsMap);
        log.info(CalibrationPlatform.LOCAL + " regression: " + this.localRegressionFunction);

        this.providerRegressionFunction = RegressionComputation.computeRegression(providerMetricsMap);
        log.info("Provider regression: " + this.providerRegressionFunction);
    }

    public Map<Integer, Double> computeMapping(List<Integer> memorySizes, Map<Double, List<Double>> localMetricsMap, Map<Double, List<Double>> providerMetricsMap) {

        this.computeFunctions(localMetricsMap, providerMetricsMap);

        final Map<Integer, Double> memorySettingCPUShare = new HashMap<>();
        for (final Integer memorySize : memorySizes) {
            double cpuShare = this.localRegressionFunction.computeDependentResult(this.providerRegressionFunction, memorySize);
            log.info("Compute CPU quota for memory size: " + memorySize + " CPU share: " + cpuShare);
            memorySettingCPUShare.put(memorySize, cpuShare);
        }
        return memorySettingCPUShare;
    }
}
