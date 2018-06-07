package de.uniba.dsg.serverless.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class FileSizeEnlarger {

	// File is written in blocks of BLOCK_SIZE bytes to avoid limited Heap
	private final long BLOCK_SIZE = 10_000_000l;

	private final Path path;
	private final long fileSize;

	/**
	 * Creates a new FileSizeEnlarger
	 * @param path
	 * @throws SeMoDeException
	 */
	public FileSizeEnlarger(Path path) throws SeMoDeException {
		this.path = path;
		try {
			this.fileSize = Files.size(path);
		} catch (IOException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}
	}

	/**
	 * Fills a file to a specified size starting with the specified commentStart escapeSequence
	 * 
	 * @param desiredFileSize
	 * @param commentStart
	 * @throws SeMoDeException
	 */
	public void fillRegularFile(long desiredFileSize, String commentStart) throws SeMoDeException {
		long remainingSize = desiredFileSize - fileSize;

		String content = "\n" + commentStart;
		addContent(path, content);
		remainingSize -= content.length();

		for (int i = 0; i < remainingSize / BLOCK_SIZE; i++) {
			addRandomContentToFile(path, BLOCK_SIZE);
			remainingSize -= BLOCK_SIZE;
		}
		addRandomContentToFile(path, remainingSize);
	}

	/**
	 * Iteratively fills a Zip File to a specified size. 
	 * 
	 * @see <a href=
	 *      "https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/zipfilesystemprovider.html">Zip
	 *      File System Provider</a>
	 * @param path
	 * @throws SeMoDeException
	 */
	public void fillZip(long length) throws SeMoDeException {
		URI uri = URI.create("jar:" + path.toUri());
		for (int i = 0; i < length / BLOCK_SIZE; i++) {
			try (FileSystem system = FileSystems.newFileSystem(uri, new HashMap<>())) {
				addRandomContentToFile(system.getPath("/SomeTextFile.txt"), BLOCK_SIZE);
			} catch (IOException | FileSystemNotFoundException e) {
				throw new SeMoDeException(e.getMessage(), e);
			}
		}
		try {
			while (length > Files.size(path)) {
				try (FileSystem system = FileSystems.newFileSystem(uri, new HashMap<>())) {
					long nextPackage = length - Files.size(path);
					addRandomContentToFile(system.getPath("/SomeTextFile.txt"), nextPackage);
				}
			}
		} catch (IOException | FileSystemNotFoundException e) {
			throw new SeMoDeException(e.getMessage(), e);
		}
	}

	private void addContent(Path path, String content) throws SeMoDeException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
				StandardOpenOption.CREATE)) {
			writer.write(content);
			writer.flush();
		} catch (IOException e) {
			throw new SeMoDeException("Writing to file failed.", e);
		}
	}

	private void addRandomContentToFile(Path path, long length) throws SeMoDeException {
		String st = RandomStringUtils.randomAlphanumeric((int) length);
		addContent(path, st);
	}

}
