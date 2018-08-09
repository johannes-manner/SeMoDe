package de.uniba.dsg.serverless.pipeline.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkSetup {

	public static final String SETUP_LOCATION = "setups";

	private List<Provider> provider = new ArrayList<>();
	private List<Integer> awsMemorySettings = new ArrayList<>();
	private List<Integer> deploymentSizes = new ArrayList<>();
	private List<String> languages = new ArrayList<>();

	public final String name;
	public final Path pathToSetup;

	public BenchmarkSetup(String name) {
		this.name = name;
		this.pathToSetup = Paths.get(BenchmarkSetup.SETUP_LOCATION, this.name);
	}

}
