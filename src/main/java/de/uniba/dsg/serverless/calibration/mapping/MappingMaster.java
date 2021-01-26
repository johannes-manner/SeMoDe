package de.uniba.dsg.serverless.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MappingMaster {

    private final MappingCalibrationConfig config;

    private SimpleFunction localRegressionFunction;
    private SimpleFunction providerRegressionFunction;

    public MappingMaster(final MappingCalibrationConfig config) throws SeMoDeException {
        this.config = config;
        this.computeFunctions();
    }

    public void computeFunctions() throws SeMoDeException {
        final RegressionComputation localRegression = new RegressionComputation(Paths.get(this.config.localCalibrationFile));
        this.localRegressionFunction = localRegression.computeRegression();
        log.info(CalibrationPlatform.LOCAL + " regression: " + this.localRegressionFunction);

        final RegressionComputation providerRegression = new RegressionComputation(Paths.get(this.config.providerCalibrationFile));
        this.providerRegressionFunction = providerRegression.computeRegression();
        log.info("Provider regression: " + this.providerRegressionFunction);
    }

    public void computeMapping() {
        final Map<Integer, Double> memorySettingCPUShare = new HashMap<>();
        for (final Integer memorySize : this.config.getMemorySizeList()) {
            log.info("Compute CPU quota for memory size: " + memorySize);
            memorySettingCPUShare.put(memorySize, this.localRegressionFunction.computeDependentResult(this.providerRegressionFunction, memorySize));
        }
        this.config.memorySizeCPUShare = memorySettingCPUShare;
    }
}
