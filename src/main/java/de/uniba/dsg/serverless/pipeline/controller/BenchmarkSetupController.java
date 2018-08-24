package de.uniba.dsg.serverless.pipeline.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import de.uniba.dsg.serverless.cli.PipelineSetupUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.model.BenchmarkSetup;
import de.uniba.dsg.serverless.pipeline.model.ProviderConfig;
import de.uniba.dsg.serverless.pipeline.utils.BenchmarkingCommandGenerator;
import de.uniba.dsg.serverless.pipeline.utils.EndpointExtractor;

public class BenchmarkSetupController {

	private static final Logger logger = LogManager.getLogger(BenchmarkSetupController.class.getName());
	private static final String SEMODE_JAR_LOCATION = "../../../../build/libs/SeMoDe.jar";
	private final ObjectMapper om;

	private final BenchmarkSetup setup;

	private BenchmarkSetupController(BenchmarkSetup setup) {
		this.setup = setup;
		om = new ObjectMapper();
	}

	public static BenchmarkSetupController init(BenchmarkSetup setup) throws SeMoDeException {
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		controller.createBenchmarkFolderStructure();
		return controller;
	}

	public static BenchmarkSetupController load(BenchmarkSetup setup) throws SeMoDeException {
		BenchmarkSetupController controller = new BenchmarkSetupController(setup);
		if (!Files.isDirectory(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup does not exist.");
		}
		setup.loadUserConfig(setup.pathToConfig.toString());
		return controller;
	}

	private void createBenchmarkFolderStructure() throws SeMoDeException {
		if (Files.exists(setup.pathToSetup)) {
			throw new SeMoDeException("Test setup already exists. Choose a different name.");
		}
		try {
			Files.createDirectories(setup.pathToSetup);
			Files.createDirectories(setup.pathToDeployment);
			Files.createDirectories(setup.pathToEndpoints);
			Files.createDirectories(setup.pathToBenchmarkingCommands);
		} catch (IOException e) {
			throw new SeMoDeException(e);
		}
	}

	public void configureBenchmarkSetup() {
		String provider;
		do {
			System.out.println("Insert a valid provider: " + setup.possibleProviders.keySet().toString());
			provider = PipelineSetupUtility.scanner.nextLine();
			if (setup.possibleProviders.containsKey(provider)) {
				List<String> providerJson = readProviderProperties(provider);
				String json = "{" + providerJson.stream().collect(Collectors.joining(",")) + "}";
				try {
					ProviderConfig p = om.readValue(json, ProviderConfig.class);
					p.validate(setup.possibleProviders);
					setup.userProviders.put(p.getName(), p);
					
					// auto save
					this.saveBenchmarkSetup();
				} catch (IOException e) {
					System.err.println("Incorrect json format: " + json);
				} catch (SeMoDeException e) {
					System.err.println("Incorrect property value: " + e.getMessage());
				}
			}
		} while (!"".equals(provider));

	}

	private List<String> readProviderProperties(String provider) {

		List<String> providerProperties = new ArrayList<>();
		for (String key : ProviderConfig.jsonProviderProperties()) {
			printPropertyPrompt(key);
			String line = PipelineSetupUtility.scanner.nextLine();
			providerProperties.add("\"" + key + "\": " + line);
			logger.info("Successfully stored property " + key);
		}
		return providerProperties;
	}

	private void printPropertyPrompt(String key) {
		System.out.println("Configure property \"" + key + "\"");
		System.out.println(
				"Please specify the property. Think about the correct JSON representation for the value. \n(empty to skip property)");
	}

	private void saveBenchmarkSetup() throws SeMoDeException {

		try {
			om.writer().withDefaultPrettyPrinter().writeValue(Paths.get(setup.pathToConfig.toString()).toFile(),
					setup.assembleUserConfig());
		} catch (IOException e) {
			throw new SeMoDeException("Configuration could not be saved.", e);
		}

	}

	public void printBenchmarkSetupStatus() throws SeMoDeException {
		System.out.println("Printing status of benchmark setup \"" + setup.name + "\"");
		System.out.println("Printing Properties:");
		System.out.println(setup.assembleUserConfig());
	}

	public void prepareDeployment() throws SeMoDeException {

		System.out.println("copying sources...");
		for (String provider : setup.userProviders.keySet()) {
			for (String language : setup.userProviders.get(provider).getLanguage()) {
				copySource(provider, language);
			}
		}

		// TODO run a single process builder for each provider / language combination
		String bashExeLocation = "C:\\Program Files\\Git\\bin\\bash.exe";

		// create Deployments
		System.out.println("creating deployment sizes");
		executeBashCommand(bashExeLocation, "bash createDeployments " + SEMODE_JAR_LOCATION, "-preparation");

		// deployment
		System.out.println("Deploying created functions... (may take a while)");
		executeBashCommand(bashExeLocation, "bash deploy", "-deploy");

	}

	private void executeBashCommand(String bashExeLocation, String command, String fileSuffix) throws SeMoDeException {
		for (String provider : setup.userProviders.keySet()) {
			for (String language : setup.userProviders.get(provider).getLanguage()) {
				ProcessBuilder processBuilder = new ProcessBuilder(bashExeLocation, "-c", command);
				String providerLanguage = provider + "-" + language;
				Path sourceLocation = Paths.get(setup.pathToSetup.toString(), "sources", providerLanguage);
				processBuilder.directory(sourceLocation.toFile());
				processBuilder.redirectErrorStream(true);
				Process process = null;
				try {
					process = processBuilder.start();
					this.writeProcessOutputToFile(process, providerLanguage + fileSuffix);
					int errCode = process.waitFor();
					System.out
							.println("Executed without errors? " + (errCode == 0 ? "Yes" : "No(code=" + errCode + ")"));
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					process.destroy();
				}
			}
		}
	}

	private void writeProcessOutputToFile(Process process, String fileName) throws SeMoDeException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(setup.pathToDeployment.resolve(fileName).toString())))) {

			String line;
			while ((line = br.readLine()) != null) {
				bw.write(line + System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SeMoDeException("Error while writing the output of the deploymentscript to the file");
		}
	}

	private void copySource(String provider, String language) throws SeMoDeException {
		String sourceFolderName = provider + "-" + language;
		File source = new File(Paths.get("fibonacci", sourceFolderName).toString());
		File target = new File(setup.pathToSources.resolve(sourceFolderName).toString());
		try {
			FileUtils.copyDirectory(source, target);
		} catch (IOException e) {
			throw new SeMoDeException("Copying the source of " + sourceFolderName + "failed.", e);
		}
	}

	public void generateEndpoints() throws SeMoDeException {
		EndpointExtractor endpointExtractor = new EndpointExtractor(setup.config.getLanguageConfigMap(), setup.pathToDeployment, setup.pathToEndpoints);
		for (String provider : setup.userProviders.keySet()) {
			for (String language : setup.userProviders.get(provider).getLanguage()) {
				endpointExtractor.extractEndpoints(language, provider);
			}
		}
	}

	public void generateBenchmarkingCommands() throws SeMoDeException {
		
		System.out.println("Insert a supported benchmarking mode");
		String benchmarkingMode = PipelineSetupUtility.scanner.nextLine();
		System.out.println("Insert benchmarking parameters");
		String benchmarkingParameters = PipelineSetupUtility.scanner.nextLine();
		System.out.println("Copy a file called 'params.json' in the 'benchmarkingCommands' folder");
		
		BenchmarkConfig config = new BenchmarkConfig(benchmarkingMode,benchmarkingParameters);
		setup.benchmarkConfig = config;
		
		// auto save to store the benchmark
		this.saveBenchmarkSetup();
		
		BenchmarkingCommandGenerator bcg = new BenchmarkingCommandGenerator(setup.pathToBenchmarkingCommands, setup.pathToEndpoints, setup.benchmarkConfig);
		
		for (String provider : setup.userProviders.keySet()) {
			for (String language : setup.userProviders.get(provider).getLanguage()) {
				bcg.generateCommands(language, provider);
			}
		}
	}
}
