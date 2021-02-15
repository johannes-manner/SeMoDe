package de.uniba.dsg.serverless.pipeline.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;

public class FileSizeEnlarger {

    // File is written in blocks of BLOCK_SIZE bytes to avoid limited Heap
    private final long BLOCK_SIZE = 10_000_000l;

    private final Path path;
    private final long fileSize;

    /**
     * Creates a new FileSizeEnlarger
     *
     * @param path
     * @throws SeMoDeException
     */
    public FileSizeEnlarger(final Path path) throws SeMoDeException {
        this.path = path;
        try {
            this.fileSize = Files.size(path);
        } catch (final IOException e) {
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
    public void fillRegularFile(final long desiredFileSize, final String commentStart) throws SeMoDeException {
        long remainingSize = desiredFileSize - this.fileSize;

        final String content = "\n" + commentStart;
        this.addContent(this.path, content);
        remainingSize -= content.length();

        for (int i = 0; i < remainingSize / this.BLOCK_SIZE; i++) {
            this.addRandomContentToFile(this.path, this.BLOCK_SIZE);
            remainingSize -= this.BLOCK_SIZE;
        }
        this.addRandomContentToFile(this.path, remainingSize);
    }

    /**
     * Iteratively fills a Zip File to a specified size.
     *
     * @throws SeMoDeException
     * @see <a href=
     * "https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/zipfilesystemprovider.html">Zip
     * File System Provider</a>
     */
    public void fillZip(final long length) throws SeMoDeException {
        final long remainingSize = length - this.fileSize;

        final URI uri = URI.create("jar:" + this.path.toUri());
        for (int i = 0; i < remainingSize / this.BLOCK_SIZE; i++) {
            try (final FileSystem system = FileSystems.newFileSystem(uri, new HashMap<>())) {
                this.addRandomContentToFile(system.getPath("/SomeTextFile.txt"), this.BLOCK_SIZE);
            } catch (final IOException | FileSystemNotFoundException e) {
                throw new SeMoDeException(e.getMessage(), e);
            }
        }
        try {
            while (length > Files.size(this.path)) {
                try (final FileSystem system = FileSystems.newFileSystem(uri, new HashMap<>())) {
                    final long nextPackage = length - Files.size(this.path);
                    this.addRandomContentToFile(system.getPath("/SomeTextFile.txt"), nextPackage);
                }
            }
        } catch (final IOException | FileSystemNotFoundException e) {
            throw new SeMoDeException(e.getMessage(), e);
        }
    }

    private void addContent(final Path path, final String content) throws SeMoDeException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
                StandardOpenOption.CREATE)) {
            writer.write(content);
            writer.flush();
        } catch (final IOException e) {
            throw new SeMoDeException("Writing to file failed.", e);
        }
    }

    private void addRandomContentToFile(final Path path, final long length) throws SeMoDeException {
        final String st = RandomStringUtils.randomAlphanumeric((int) length);
        this.addContent(path, st);
    }

}
