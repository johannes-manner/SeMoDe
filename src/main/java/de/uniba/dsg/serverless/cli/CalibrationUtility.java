package de.uniba.dsg.serverless.cli;

import com.google.common.collect.Maps;
import de.uniba.dsg.serverless.calibration.CalibrationCommand;
import de.uniba.dsg.serverless.calibration.CalibrationPlatform;
import de.uniba.dsg.serverless.calibration.aws.AWSCalibration;
import de.uniba.dsg.serverless.calibration.aws.AWSConfig;
import de.uniba.dsg.serverless.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.calibration.local.ResourceLimit;
import de.uniba.dsg.serverless.calibration.profiling.ContainerExecutor;
import de.uniba.dsg.serverless.model.SeMoDeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CalibrationUtility extends CustomUtility {

    private static final Logger logger = LogManager.getLogger(CalibrationUtility.class.getName());

    private CalibrationCommand subcommand;

    // calibration
    private CalibrationPlatform platform;
    private String calibrationName;
    // TODO make it provider independent (for later experiments)
    private AWSConfig awsConfig;

    // mapping

    // runContainer
    private ContainerExecutor containerExecutor;
    public static final Path PROFILING_PATH = Paths.get("profiling");
    private Path profilingOutputFolder;
    private Map<String, String> environmentVariables;
    private ResourceLimit limits;

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
                    break;
                case RUN_CONTAINER:
                    containerExecutor.runContainer(environmentVariables, limits);
                    containerExecutor.saveProfile(profilingOutputFolder);
                    logger.info("Profile saved in " + profilingOutputFolder);
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
                throw new SeMoDeException("Info Utility not yet supported.");
            case RUN_CONTAINER:
                validateArgumentSize(arguments, 3);
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

                String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                profilingOutputFolder = PROFILING_PATH.resolve("profiling_" + time);

                // TODO Replace this with calibration. (see Note in documentation)
                limits = new ResourceLimit(1.5, false, 300);
                break;
        }
    }

    private void printUsage() {
        logger.info("Usage: Choose one of three subcommands. \"calibrate\", \"info\" or \"runContainer\".");
        logger.info("calibration calibration calibrate PLATFORM CALIBRATION_NAME");
        logger.info("calibration info PROVIDER_CALIBRATION LOCAL_CALIBRATION ..(TBD)");
        logger.info("calibration runContainer IMAGE_NAME ENV_FILE");
    }
}
