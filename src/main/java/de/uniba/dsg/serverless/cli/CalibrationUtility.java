package de.uniba.dsg.serverless.cli;

import com.google.common.collect.Maps;
import de.uniba.dsg.serverless.calibration.Calibration;
import de.uniba.dsg.serverless.calibration.CalibrationCommand;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibration;
import de.uniba.dsg.serverless.calibration.aws.AWSConfig;
import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.calibration.mapping.MappingMaster;
import de.uniba.dsg.serverless.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalibrationUtility extends CustomUtility {

    private static final Logger logger = LogManager.getLogger(CalibrationUtility.class.getName());

    private CalibrationCommand subcommand;

    // calibration
    private CalibrationPlatform platform;
    private String calibrationName;
    // TODO make it provider independent (for later experiments)
    private AWSConfig awsConfig;

    // mapping
    private Path localCalibrationFile;
    private Path providerCalibrationFile;

    // runContainer
    private ContainerExecutor containerExecutor;
    public static final Path PROFILING_PATH = Paths.get("profiling");
    private Map<String, String> environmentVariables;
    private ResourceLimit limits;
    private int numberOfProfiles;

    public CalibrationUtility(String name) {
        super(name);
    }

    @Override
    public void start(List<String> args) {
        try {
            parseArguments(args);
            switch (subcommand) {
                case PERFORM_CALIBRATION:
                    if (platform == CalibrationPlatform.LOCAL) {
                        new LocalCalibration(calibrationName).performCalibration();
                    } else if (platform == CalibrationPlatform.AWS) {
                        new AWSCalibration(calibrationName).performCalibration(awsConfig);
                    }
                    break;
                case MAPPING:
                    new MappingMaster(this.localCalibrationFile, this.providerCalibrationFile).computeMapping(1337.0);
                    break;
                case RUN_CONTAINER:
                    containerExecutor.executeLocalProfiles(environmentVariables, limits, numberOfProfiles);
                    break;
            }
        } catch (SeMoDeException e) {
            logger.fatal("Could not perform calibration. " + e.getMessage(), e);
            printUsage();
        }
    }

    private void validateArgumentSize(List<String> arguments, int argumentSize) throws SeMoDeException {
        if (arguments.size() < argumentSize) {
            throw new SeMoDeException("Expected at least " + argumentSize + " arguments. Given only: " + arguments.size());
        }
    }

    private void parseArguments(List<String> arguments) throws SeMoDeException {
        subcommand = CalibrationCommand.fromString(arguments.get(0));
        switch (subcommand) {
            case PERFORM_CALIBRATION:
                validateArgumentSize(arguments, 3);
                platform = CalibrationPlatform.fromString(arguments.get(1));
                calibrationName = arguments.get(2);
                if (calibrationName.isEmpty() || calibrationName.contains("/") || calibrationName.contains(".")) {
                    throw new SeMoDeException("Illegal filename " + calibrationName);
                }
                awsConfig = AWSConfig.fromFile("aws_calibration.json");
                break;
            case MAPPING:
                validateArgumentSize(arguments, 3);
                this.localCalibrationFile = Paths.get(arguments.get(1));
                this.providerCalibrationFile = Paths.get(arguments.get(2));
                if (!Files.exists(providerCalibrationFile) || !Files.exists(localCalibrationFile)) {
                    throw new SeMoDeException("Calibration file missing. Please check CLI arguments. Path: calibration/PLATFORM/CALIBRATION_FILE");
                }
                if (!localCalibrationFile.startsWith(Calibration.CALIBRATION_FILES) || !providerCalibrationFile.startsWith(Calibration.CALIBRATION_FILES)) {
                    throw new SeMoDeException("Calibration file must be in calibration/");
                }
                break;
            case RUN_CONTAINER:
                validateArgumentSize(arguments, 7);
                String imageName = arguments.get(1);
                String tag = "semode/" + imageName;
                String dockerfile = PROFILING_PATH.resolve(imageName).resolve("Dockerfile").toString();
                containerExecutor = new ContainerExecutor(tag, dockerfile, true);

                Path envFile = PROFILING_PATH.resolve(imageName).resolve(arguments.get(2));
                Properties properties = new Properties();
                try {
                    properties.load(new FileInputStream(envFile.toString()));
                    environmentVariables = Maps.fromProperties(properties);
                } catch (IOException e) {
                    throw new SeMoDeException("Could not load environment variables from " + envFile + ".", e);
                }

                this.localCalibrationFile = Paths.get(arguments.get(3));
                this.providerCalibrationFile = Paths.get(arguments.get(4));
                try {
                    int simulatedMemory = Integer.parseInt(arguments.get(5));
                    double quota = new MappingMaster(localCalibrationFile, providerCalibrationFile).computeMapping(simulatedMemory);
                    limits = new ResourceLimit(quota, false, simulatedMemory);
                    this.numberOfProfiles = Integer.parseInt(arguments.get(6));
                } catch (NumberFormatException e) {
                    throw new SeMoDeException("Simulated Memory and number of profiles must be Integers.", e);
                }
                break;
        }
    }

    private void printUsage() {
        logger.info("Usage: Choose one of three subcommands. \"calibrate\", \"mapping\" or \"runContainer\".");
        logger.info("calibration calibration calibrate PLATFORM CALIBRATION_NAME");
        logger.info("calibration mapping LOCAL_CALIBRATION PROVIDER_CALIBRATION");
        logger.info("calibration runContainer IMAGE_NAME ENV_FILE LOCAL_CALIBRATION PROVIDER_CALIBRATION SIMULATED_MEMORY NUMBER_OF_PROFILES");
    }
}
