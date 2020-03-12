package de.uniba.dsg.serverless.calibration.mapping;

import de.uniba.dsg.serverless.pipeline.model.SupportedPlatform;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class MappingMaster {

    private static final Logger logger = LogManager.getLogger(MappingMaster.class.getName());

    private final Path localCalibrationFile;
    private final Path providerCalibrationFile;

    private SimpleFunction localRegressionFunction;
    private SimpleFunction providerRegressionFunction;


    public MappingMaster(Path localCalibrationFile, Path providerCalibrationFile) throws SeMoDeException {
        this.localCalibrationFile = localCalibrationFile;
        this.providerCalibrationFile = providerCalibrationFile;
        computeFunctions();
    }


    public void computeFunctions() throws SeMoDeException {
        RegressionComputation localRegression = new RegressionComputation(localCalibrationFile);
        localRegressionFunction = localRegression.computeRegression();
        logger.info(SupportedPlatform.LOCAL + " " + localRegressionFunction);

        RegressionComputation providerRegression = new RegressionComputation(providerCalibrationFile);
        providerRegressionFunction = providerRegression.computeRegression();
        logger.info(SupportedPlatform.AWS + " " + providerRegressionFunction);
    }

    public double computeMapping(double memorySetting) {
        return localRegressionFunction.computeDependentResult(providerRegressionFunction, memorySetting);
    }
}
