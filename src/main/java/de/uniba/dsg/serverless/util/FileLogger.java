package de.uniba.dsg.serverless.util;

import de.uniba.dsg.serverless.ArgumentProcessor;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileLogger {

    protected final FileHandler fileHandler;
    private final Logger logger;

    public FileLogger(final String name, final String path, final boolean isGlobalLogger) {
        this.logger = Logger.getLogger(name);
        try {
            this.fileHandler = new FileHandler(path, true);
            this.fileHandler.setFormatter(new SimpleFormatter());

            this.logger.addHandler(this.fileHandler);
            // add also the global file handler to each created logger
            // no need to log messages twice :)
            if (!isGlobalLogger) {
                this.logger.addHandler(ArgumentProcessor.logger.fileHandler);
            }
        } catch (final IOException e) {
            System.err.println("Could not create logger");
            // currently no better idea
            throw new RuntimeException(e);
        }
    }

    public void info(final String message) {
        this.logger.info(message);
    }

    public void warning(final String message) {
        this.logger.warning(message);
    }
}
