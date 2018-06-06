package de.uniba.dsg.serverless.cli;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class DeploymentSizeUtility extends CustomUtility {

	private final char[] FILL_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
			.toCharArray();
	private final String DOUBLE_LINEBREAK = "\n\n";
	private final long BLOCK_SIZE = 10_000_000l;

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

		Path path;
		long fileSize;
		try {
			path = Paths.get(fileName);
			if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
				logger.fatal("File must be an existing readable file.");
				return;
			}
			fileSize = Files.size(path);
		} catch (InvalidPathException e) {
			logger.fatal("Path is invalid.", e);
			return;
		} catch (IOException e) {
			logger.fatal("Filesize could not be determined.", e);
			return;
		}
		if (fileSize > desiredLength) {
			logger.fatal("Length of the file exceeds the desired length.");
			return;
		}

		long remainingLength = desiredLength - fileSize;
		try {
			String content = DOUBLE_LINEBREAK + commentStart;
			addContent(path, content);
			remainingLength -= content.length();
			addRandomContentToFile(path, remainingLength);
		} catch (SeMoDeException e) {
			logger.fatal(e.getMessage());
			return;
		}
		logger.info("Size of file " + fileName + " successfully increased to " + desiredLength + ".");
	}

	private void addContent(Path path, String content) throws SeMoDeException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
				StandardOpenOption.CREATE)) {
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new SeMoDeException("Writing to file failed.", e);
		}
	}

	private void addRandomContentToFile(Path path, long length) throws SeMoDeException {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();

		while (length > 0) {
			int index = random.nextInt(FILL_CHARACTERS.length);
			sb.append(FILL_CHARACTERS[index]);
			length--;
		}
		addContent(path, sb.toString());
	}

	/**
	 * 
	 * @see <a href=
	 *      "https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/zipfilesystemprovider.html">Zip
	 *      File System Provider</a>
	 * @param path
	 * @throws SeMoDeException
	 */
	private void fillZip(String path, long length) throws SeMoDeException {
		Path zipPath = Paths.get(path);
		URI uri = URI.create("jar:" + zipPath.toUri());
		try (FileSystem system = FileSystems.newFileSystem(uri, new HashMap<>())) {
			Path pathInZipfile = system.getPath("/SomeTextFile.txt");
			for (int i = 0; i < length / BLOCK_SIZE; i++) {
				addRandomContentToFile(pathInZipfile, BLOCK_SIZE);
			}
		} catch (IOException | FileSystemNotFoundException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}
		
		try {
			while (length > Files.size(zipPath)) {
				try (FileSystem system = FileSystems.newFileSystem(uri, new HashMap<>())) {
					Path pathInZipfile = system.getPath("/SomeTextFile.txt");
					long nextPackage = length - Files.size(zipPath);
					addRandomContentToFile(pathInZipfile, nextPackage);
				}
			}
		} catch (IOException|FileSystemNotFoundException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}

	}

}
