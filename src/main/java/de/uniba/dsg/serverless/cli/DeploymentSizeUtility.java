package de.uniba.dsg.serverless.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.util.FileSizeEnlarger;

public class DeploymentSizeUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(DeploymentSizeUtility.class.getName());

	private boolean isZipFile;
	private Path path;
	private long desiredFileSize;
	private String commentStart;

	public DeploymentSizeUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		try {
			parseArguments(args);

			FileSizeEnlarger enlarger = new FileSizeEnlarger(path);
			if (isZipFile) {
				enlarger.fillZip(desiredFileSize);
			}
			else {
				enlarger.fillRegularFile(desiredFileSize, commentStart);
			}
		} catch (SeMoDeException e) {
			logger.fatal("Increasing the Size of the file failed. " + e.getMessage(), e);
		}
	}

	private void parseArguments(List<String> arguments) throws SeMoDeException {
		if (arguments.size() < 2) {
			throw new SeMoDeException("Wrong parameter size: " + "\n(1) path " + "\n(2) desired length in bytes "
					+ "\n(3) Comment Start Tag [specified when a file is supplied]");
		}

		try {
			desiredFileSize = Long.parseLong(arguments.get(1));
		} catch (NumberFormatException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}

		try {
			String fileName = arguments.get(0);
			path = Paths.get(fileName);
			if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
				throw new SeMoDeException("File must be an existing readable file or zipFile.");
			}
			isZipFile = fileName.toLowerCase().endsWith(".zip") || fileName.toLowerCase().endsWith(".jar");
			if (desiredFileSize <= Files.size(path)) {
				throw new SeMoDeException("Desired File Size must be larger than current file Size.");
			}
		} catch (InvalidPathException | IOException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}

		if (!isZipFile && arguments.size() < 3) {
			throw new SeMoDeException("No comment escape sequence provided");
		}
		if (!isZipFile) {
			commentStart = arguments.get(2);
		}
	}
}
