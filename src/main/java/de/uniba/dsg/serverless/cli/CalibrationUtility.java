package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationCommand;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CalibrationUtility extends CustomUtility {

    private static final Logger logger = LogManager.getLogger(CalibrationUtility.class.getName());

    private CalibrationPlatform platform;
    private String calibrationName;
    private CalibrationCommand command;


    public CalibrationUtility(String name) {
        super(name);
    }

    @Override
    public void start(List<String> args) {
        try {
            parseArguments(args);
            switch (command) {
                case INFO:
                    break;
                case PERFORM_CALIBRATION:
                    new Calibration(platform, calibrationName).calibrate();
                    break;
            }
        } catch (SeMoDeException e) {
            logger.fatal("Could not perform calibration. " + e.getMessage(), e);
            printUsage();
        }
    }

    private void parseArguments(List<String> arguments) throws SeMoDeException {
        if (arguments.size() < 3) {
            throw new SeMoDeException("Expected at least 3 arguments. Given only: " + arguments.size());
        }
        command = CalibrationCommand.fromString(arguments.get(0));

        platform = CalibrationPlatform.fromString(arguments.get(1));

        calibrationName = arguments.get(2);
        if (calibrationName.isEmpty() || calibrationName.contains("/") || calibrationName.contains(".")) {
            throw new SeMoDeException("Illegal filename " + calibrationName);
        }
    }

    private void printUsage() {
        logger.info("Usage: (\"calibrate\" | \"info\") PLATFORM CALIBRATION_FILE");
    }

}
