package de.uniba.dsg.serverless.pipeline.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
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

    // TODO document
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
        this.localRegressionFunction.computeCPUMemoryEquivalents(this.providerRegressionFunction);
        return memorySettingCPUShare;
    }

    public Map<Integer, Integer> computeCPUMemoryEquivalents(int noOfCPUs, Map<Double, List<Double>> localMetricsMap, Map<Double, List<Double>> providerMetricsMap) {

        this.computeFunctions(localMetricsMap, providerMetricsMap);

        return this.localRegressionFunction.computeCPUMemoryEquivalents(noOfCPUs, this.providerRegressionFunction);
    }


    /**
     * Return a map in the form GFLOP,ResourceSetting
     *
     * @param gflops
     * @param providerMetricsMap
     * @return
     */
    public Map<Double, Integer> computeGflopMapping(List<Double> gflops, Map<Double, List<Double>> providerMetricsMap, CalibrationPlatform platform) throws SeMoDeException {
        log.info("Compute GFLOPS for platform: " + platform.getText());
        Map<Double, Integer> result = new HashMap<>();
        SimpleFunction providerFunction = RegressionComputation.computeRegression(providerMetricsMap);
        for (Double gflop : gflops) {
            if (platform == CalibrationPlatform.AWS) {
                // use the already stored memory settings
                result.put(gflop, (int) (providerFunction.computeX(gflop) + 0.5));
            } else if (platform == CalibrationPlatform.OPEN_FAAS) {
                // make cpu shares to 100m compliant shares for K8s deployment
                result.put(gflop, (int) (providerFunction.computeX(gflop) * 1000 + 0.5));
            } else {
                throw new SeMoDeException("Platform " + platform.getText() + " not supported");
            }
        }
        return result;
    }
}
