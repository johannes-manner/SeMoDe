package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.pipeline.util.FileSizeEnlarger;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class DeploymentSizeUtility implements CustomUtility {

    public static final int BYTES_OF_KB = 1_024;
    public static final String TEMP_DEPLOYMENT_FOLDER = "deployment";
    private boolean isZipFile;
    private Path path;
    private long desiredFileSize;
    private String commentStart;

    @Override
    public String getName() {
        return "deploymentSize";
    }

    /**
     * This method enlarges a file and uses this file for deployment.
     * Important for testing the deployment size hypotheses for cold starts <br/><br/>
     * <p>
     * 1. Parameter: Path <br/>
     * 2. Parameter: Length in KB <br/>
     * 3. Parameter: Comment, e.g. for Java/JS // <br/>
     *
     * @param args
     */
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

    public void copyZipAndEnlarge(String source, String functionName) throws SeMoDeException {
        log.info("Enlarge source file " + source + ", function name: " + functionName);
        // original source
        Path sourcePath = Paths.get(source);
        // copy it to a temp location
        String tempFileName = DeploymentSizeUtility.TEMP_DEPLOYMENT_FOLDER + "/" + functionName + ".zip";
        try {
            Files.deleteIfExists(Paths.get(tempFileName));
            Files.copy(sourcePath, Paths.get(tempFileName));
        } catch (IOException e) {
            throw new SeMoDeException(e);
        }

        // enlarge it
        String deploymentSize = functionName.split("_")[functionName.split("_").length - 1];
        this.start(List.of(tempFileName, deploymentSize, "//"));
    }

    private void parseArguments(final List<String> arguments) throws SeMoDeException {
        if (arguments.size() < 2) {
            throw new SeMoDeException("Wrong parameter size: " + "\n(1) path " + "\n(2) desired length in bytes "
                    + "\n(3) Comment Start Tag [specified when a file is supplied]");
        }

        try {
            this.desiredFileSize = Long.parseLong(arguments.get(1)) * BYTES_OF_KB;
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
