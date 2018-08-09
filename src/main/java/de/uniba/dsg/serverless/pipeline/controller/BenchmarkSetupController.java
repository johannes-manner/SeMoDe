package de.uniba.dsg.serverless.pipeline.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;

public class BenchmarkSetupController {

	private final BenchmarkSetup setup;

	public BenchmarkSetupController(BenchmarkSetup setup) {
		this.setup = setup;

	}

	public void initBenchmark() throws SeMoDeException {
		if (Files.exists(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup already exists. Choose a different name.");
		}
		try {
			Files.createDirectories(setup.pathToSetup);
		} catch (IOException e) {
			throw new SeMoDeException(e);
		}
		gitIgnoreAll();
		initConfiguration();
	}

	private void initConfiguration() {

	}

	public void loadBenchmark() throws SeMoDeException {
		if (!Files.isDirectory(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		loadConfiguration();
	}

	private void loadConfiguration() {
		
	}

	private void gitIgnoreAll() throws SeMoDeException {
		Path pathToGitIgnore = Paths.get(BenchmarkSetup.SETUP_LOCATION, ".gitignore");
		if (!Files.exists(pathToGitIgnore)) {
			try {
				Files.write(pathToGitIgnore, "*".getBytes());
			} catch (IOException e) {
				throw new SeMoDeException(e);
			}
		}
	}

}
