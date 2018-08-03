package de.uniba.dsg.serverless.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class Pipeline {

	private final String SETUP_LOCATION = "setups";

	public Pipeline(String testSetupName) throws SeMoDeException {
		Path path = Paths.get(SETUP_LOCATION, testSetupName);
		if (Files.exists(path)) {
			throw new SeMoDeException("Test setup already exists. Choose a different name.");
		}
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new SeMoDeException(e);
		}
		gitIgnoreAll();
	}

	private void gitIgnoreAll() throws SeMoDeException {
		Path pathToGitIgnore = Paths.get(SETUP_LOCATION, ".gitignore");
		if (!Files.exists(pathToGitIgnore)) {
			try {
				Files.write(pathToGitIgnore, "*".getBytes());
			} catch (IOException e) {
				throw new SeMoDeException(e);
			}
		}
	}

}
