package de.uniba.dsg.serverless.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class DeploymentSizeUtility extends CustomUtility {

	private final String FILL_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private final String DOUBLE_LINEBREAK = "\n\n";

	private static final Logger logger = LogManager.getLogger(DeploymentSizeUtility.class.getName());

	public DeploymentSizeUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {
		if (args.size() < 3) {
			logger.fatal("Wrong parameter size: " + "\n(1) file path " + "\n(2) desired length in bytes "
					+ "\n(3) Comment Start Tag");
			return;
		}

		String fileName = args.get(0);
		long desiredLength;
		try {
			desiredLength = Long.parseLong(args.get(1));
		} catch (NumberFormatException e) {
			logger.fatal(e.getMessage());
			return;
		}
		String commentStart = args.get(2);

		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			logger.fatal("File must be an existing file.");
			return;
		}

		if (file.length() > desiredLength) {
			logger.fatal("Length of the file exceeds the desired length.");
			return;
		}

		long remainingLength = desiredLength - file.length();
		try {
			fillFile(file, remainingLength, commentStart);
		} catch (SeMoDeException e) {
			logger.fatal(e.getMessage());
			return;
		}
		logger.info("Size of file " + fileName + " successfully increased to " + desiredLength + ".");
	}

	private void fillFile(File file, long remainingLength, String commentStart) throws SeMoDeException {
		try (FileWriter fw = new FileWriter(file, true); BufferedWriter writer = new BufferedWriter(fw)) {
			if (remainingLength < (DOUBLE_LINEBREAK.length() + commentStart.length())) {
				while (remainingLength > 0) {
					writer.write(' ');
					remainingLength--;
				}
			}
			else {
				writer.write(DOUBLE_LINEBREAK);
				remainingLength -= DOUBLE_LINEBREAK.length();
				writer.write(commentStart);
				remainingLength -= commentStart.length();

				Random random = new Random();
				while (remainingLength > 0) {
					int index = random.nextInt(FILL_CHARACTERS.length());
					writer.write(FILL_CHARACTERS.charAt(index));
					remainingLength--;
				}
			}
		} catch (IOException e) {
			throw new SeMoDeException("Writing to file failed.", e);
		}
	}

}
