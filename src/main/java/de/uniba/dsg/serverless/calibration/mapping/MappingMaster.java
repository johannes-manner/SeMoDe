package de.uniba.dsg.serverless.calibration.mapping;

import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Map;

public class MappingMaster {

    private static final Logger logger = LogManager.getLogger(MappingMaster.class.getName());

    private final Path localCalibrationFile;
    private final Map<String, Path> providerFiles;

    public MappingMaster(Path localCalibrationFile, Map<String, Path> providerFiles) {
        this.localCalibrationFile = localCalibrationFile;
        this.providerFiles = providerFiles;
    }


    public void computeMapping() throws SeMoDeException {
        RegressionComputation localRegression = new RegressionComputation(this.localCalibrationFile);
        SimpleFunction localRegressionFunction = localRegression.computeRegression();
        logger.info(CalibrationPlatform.LOCAL + " " + localRegressionFunction);
        for(String platform : this.providerFiles.keySet()){
            RegressionComputation providerRegression = new RegressionComputation(this.providerFiles.get(platform));
            SimpleFunction providerRegressionFunction = providerRegression.computeRegression();
            logger.info(platform + " " + providerRegressionFunction);

            // TODO how to integrate the stuff... maybe include it in pipeline?...
        }
    }
}
