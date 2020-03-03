package de.uniba.dsg.serverless.cli;

import com.google.common.collect.Maps;
import de.uniba.dsg.serverless.calibration.CalibrationCommand;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibration;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibrationConfig;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CalibrationUtility extends CustomUtility {

    public static final Path PROFILING_PATH = Paths.get("profiling");
    private static final Logger logger = LogManager.getLogger(CalibrationUtility.class.getName());
    private CalibrationCommand subcommand;
    // calibration
    private CalibrationPlatform platform;
    private String calibrationName;
    // TODO make it provider independent (for later experiments)
    private AWSCalibrationConfig awsConfig;
    // mapping
    private Path localCalibrationFile;
    private Path providerCalibrationFile;
    // runContainer
    private ContainerExecutor containerExecutor;
    private Map<String, String> environmentVariables;
    private ResourceLimit limits;
    private int numberOfProfiles;

    public CalibrationUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {
        try {
            this.parseArguments(args);
            switch (this.subcommand) {
                case PERFORM_CALIBRATION:
                    if (this.platform == CalibrationPlatform.LOCAL) {
                        new LocalCalibration(this.calibrationName).performCalibration();
                    } else if (this.platform == CalibrationPlatform.AWS) {
                        new AWSCalibration(this.calibrationName).performCalibration(this.awsConfig);
                    }
                    break;
                case MAPPING:
                    // TODO remove hard-coded value here
                    new MappingMaster(this.localCalibrationFile, this.providerCalibrationFile).computeMapping(1337.0);
                    break;
                case RUN_CONTAINER:
                    this.containerExecutor.executeLocalProfiles(this.environmentVariables, this.limits, this.numberOfProfiles);
                    break;
            }
        } catch (final SeMoDeException e) {
            logger.fatal("Could not perform calibration. " + e.getMessage(), e);
            this.printUsage();
        }
    }

    private void validateArgumentSize(final List<String> arguments, final int argumentSize) throws SeMoDeException {
        if (arguments.size() < argumentSize) {
            throw new SeMoDeException("Expected at least " + argumentSize + " arguments. Given only: " + arguments.size());
        }
    }

    private void parseArguments(final List<String> arguments) throws SeMoDeException {
        this.subcommand = CalibrationCommand.fromString(arguments.get(0));
        switch (this.subcommand) {
            case PERFORM_CALIBRATION:
                this.validateArgumentSize(arguments, 3);
                this.platform = CalibrationPlatform.fromString(arguments.get(1));
                this.calibrationName = arguments.get(2);
                if (this.calibrationName.isEmpty() || this.calibrationName.contains("/") || this.calibrationName.contains(".")) {
                    throw new SeMoDeException("Illegal filename " + this.calibrationName);
                }
                this.awsConfig = AWSCalibrationConfig.fromFile("aws_calibration.json");
                break;
            case MAPPING:
                this.validateArgumentSize(arguments, 3);
                this.localCalibrationFile = Paths.get(arguments.get(1));
                this.providerCalibrationFile = Paths.get(arguments.get(2));
                if (!Files.exists(this.providerCalibrationFile) || !Files.exists(this.localCalibrationFile)) {
                    throw new SeMoDeException("Calibration file missing. Please check CLI arguments. Path: calibration/PLATFORM/CALIBRATION_FILE");
                }
                break;
            case RUN_CONTAINER:
                this.validateArgumentSize(arguments, 7);
                final String imageName = arguments.get(1);
                final String tag = "semode/" + imageName;
                final String dockerfile = PROFILING_PATH.resolve(imageName).resolve("Dockerfile").toString();
                this.containerExecutor = new ContainerExecutor(tag, dockerfile, true);

                final Path envFile = PROFILING_PATH.resolve(imageName).resolve(arguments.get(2));
                final Properties properties = new Properties();
                try {
                    properties.load(new FileInputStream(envFile.toString()));
                    this.environmentVariables = Maps.fromProperties(properties);
                } catch (final IOException e) {
                    throw new SeMoDeException("Could not load environment variables from " + envFile + ".", e);
                }

                this.localCalibrationFile = Paths.get(arguments.get(3));
                this.providerCalibrationFile = Paths.get(arguments.get(4));
                try {
                    final int simulatedMemory = Integer.parseInt(arguments.get(5));
                    final double quota = new MappingMaster(this.localCalibrationFile, this.providerCalibrationFile).computeMapping(simulatedMemory);
                    this.limits = new ResourceLimit(quota, false, simulatedMemory);
                    this.numberOfProfiles = Integer.parseInt(arguments.get(6));
                } catch (final NumberFormatException e) {
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
