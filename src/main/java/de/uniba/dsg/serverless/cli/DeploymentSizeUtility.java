package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.pipeline.util.FileSizeEnlarger;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class DeploymentSizeUtility extends CustomUtility {

    private boolean isZipFile;
    private Path path;
    private long desiredFileSize;
    private String commentStart;

    public DeploymentSizeUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {
        try {
            this.parseArguments(args);

            final FileSizeEnlarger enlarger = new FileSizeEnlarger(this.path);
            if (this.isZipFile) {
                enlarger.fillZip(this.desiredFileSize);
            } else {
                enlarger.fillRegularFile(this.desiredFileSize, this.commentStart);
            }
        } catch (final SeMoDeException e) {
            log.warn("Increasing the Size of the file failed. " + e.getMessage());
        }
    }

    private void parseArguments(final List<String> arguments) throws SeMoDeException {
        if (arguments.size() < 2) {
            throw new SeMoDeException("Wrong parameter size: " + "\n(1) path " + "\n(2) desired length in bytes "
                    + "\n(3) Comment Start Tag [specified when a file is supplied]");
        }

        try {
            this.desiredFileSize = Long.parseLong(arguments.get(1));
        } catch (final NumberFormatException e) {
            throw new SeMoDeException(e.getMessage(), e);
        }

        try {
            final String fileName = arguments.get(0);
            this.path = Paths.get(fileName);
            if (!Files.exists(this.path) || !Files.isRegularFile(this.path) || !Files.isReadable(this.path)) {
                throw new SeMoDeException("File must be an existing readable file or zipFile.");
            }
            this.isZipFile = fileName.toLowerCase().endsWith(".zip") || fileName.toLowerCase().endsWith(".jar");
            if (this.desiredFileSize <= Files.size(this.path)) {
                throw new SeMoDeException("Desired File Size must be larger than current file Size.");
            }
        } catch (final InvalidPathException | IOException e) {
            throw new SeMoDeException(e.getMessage(), e);
        }

        if (!this.isZipFile && arguments.size() < 3) {
            throw new SeMoDeException("No comment escape sequence provided");
        }
        if (!this.isZipFile) {
            this.commentStart = arguments.get(2);
        }
    }
}
