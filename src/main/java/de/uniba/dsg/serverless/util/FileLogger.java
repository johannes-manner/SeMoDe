package de.uniba.dsg.serverless.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileLogger {

    private final Logger logger;

    public FileLogger(final String name, final String path) {
        this.logger = Logger.getLogger(name);
        try {
            final FileHandler fileHandler = new FileHandler(path);
            fileHandler.setFormatter(new SimpleFormatter());

            this.logger.addHandler(fileHandler);
        } catch (final IOException e) {
            // TODO sophisticated solution
            System.err.println("Could not create logger");
        }
    }

    public void info(final String message) {
        this.logger.info(message);
    }

    public void warning(final String message) {
        this.logger.warning(message);
    }
}
