package de.uniba.dsg.serverless.pipeline.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.Provider;

public class BenchmarkSetup {

	private static final String SETUP_LOCATION = "setups";

	private List<Provider> provider;
	private List<Integer> awsMemorySettings;
	private List<Integer> deploymentSizes;
	private List<String> languages;

	private BenchmarkSetup() {
		provider = new ArrayList<>();
		awsMemorySettings = new ArrayList<>();
		deploymentSizes = new ArrayList<>();
		languages = new ArrayList<>();
	}

	public static BenchmarkSetup initialize(String name) throws SeMoDeException {
		Path path = Paths.get(BenchmarkSetup.SETUP_LOCATION, name);
		if (Files.exists(path)) {
			throw new SeMoDeException("Test setup already exists. Choose a different name.");
		}
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new SeMoDeException(e);
		}
		gitIgnoreAll();
		return new BenchmarkSetup();
	}

	public static BenchmarkSetup load(String name) throws SeMoDeException {
		Path path = Paths.get(SETUP_LOCATION, name);
		if (Files.isDirectory(path)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		return new BenchmarkSetup();
	}

	private static void gitIgnoreAll() throws SeMoDeException {
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
