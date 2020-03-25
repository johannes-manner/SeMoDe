package de.uniba.dsg.serverless.calibration.mapping;

import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.util.FileLogger;

import java.nio.file.Path;

public class MappingMaster {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private final Path localCalibrationFile;
    private final Path providerCalibrationFile;

    private SimpleFunction localRegressionFunction;
    private SimpleFunction providerRegressionFunction;


    public MappingMaster(final Path localCalibrationFile, final Path providerCalibrationFile) throws SeMoDeException {
        this.localCalibrationFile = localCalibrationFile;
        this.providerCalibrationFile = providerCalibrationFile;
        this.computeFunctions();
    }


    public void computeFunctions() throws SeMoDeException {
        final RegressionComputation localRegression = new RegressionComputation(this.localCalibrationFile);
        this.localRegressionFunction = localRegression.computeRegression();
        logger.info(SupportedPlatform.LOCAL + " " + this.localRegressionFunction);

        final RegressionComputation providerRegression = new RegressionComputation(this.providerCalibrationFile);
        this.providerRegressionFunction = providerRegression.computeRegression();
        logger.info(SupportedPlatform.AWS + " " + this.providerRegressionFunction);
    }

    public double computeMapping(final double memorySetting) {
        return this.localRegressionFunction.computeDependentResult(this.providerRegressionFunction, memorySetting);
    }
}
