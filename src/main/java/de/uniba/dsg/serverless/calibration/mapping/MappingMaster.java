package de.uniba.dsg.serverless.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.MappingCalibrationConfig;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MappingMaster {

    private final FileLogger logger;

    private final MappingCalibrationConfig config;

    private SimpleFunction localRegressionFunction;
    private SimpleFunction providerRegressionFunction;


    public MappingMaster(final MappingCalibrationConfig config, final FileLogger logger) throws SeMoDeException {
        this.config = config;
        this.logger = logger;
        this.computeFunctions();
    }


    public void computeFunctions() throws SeMoDeException {
        final RegressionComputation localRegression = new RegressionComputation(Paths.get(this.config.localCalibrationFile), this.logger);
        this.localRegressionFunction = localRegression.computeRegression();
        this.logger.info(SupportedPlatform.LOCAL + " regression: " + this.localRegressionFunction);

        final RegressionComputation providerRegression = new RegressionComputation(Paths.get(this.config.providerCalibrationFile), this.logger);
        this.providerRegressionFunction = providerRegression.computeRegression();
        this.logger.info("Provider regression: " + this.providerRegressionFunction);
    }

    public void computeMapping() {
        final Map<Integer, Double> memorySettingCPUShare = new HashMap<>();
        for (final Integer memorySize : this.config.memorySizes) {
            this.logger.info("Compute CPU quota for memory size: " + memorySize);
            memorySettingCPUShare.put(memorySize, this.localRegressionFunction.computeDependentResult(this.providerRegressionFunction, memorySize));
        }
        this.config.memorySizeCPUShare = memorySettingCPUShare;
    }
}
