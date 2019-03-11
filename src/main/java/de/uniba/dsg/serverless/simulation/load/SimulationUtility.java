package de.uniba.dsg.serverless.simulation.load;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.simulation.load.model.SimulationInput;

public class SimulationUtility extends CustomUtility {

	private static final Logger logger = LogManager.getLogger(SimulationUtility.class);

	private Path file;

	public SimulationUtility(String name) {
		super(name);
	}

	@Override
	public void start(List<String> args) {

		if (args == null || args.size() != 2) {
			logger.fatal("The provided element size is not correct! " + args);
			return;
		}

		try {
			this.file = Paths.get(args.get(0));
			List<SimulationInput> simulationInput = new ObjectMapper().readValue(args.get(1),
					new TypeReference<List<SimulationInput>>() {
					});
			this.simulate(simulationInput);

		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Error reading simulation input json! " + args.get(1));
			return;
		} catch (SeMoDeException e) {
			logger.fatal(e.getMessage());
			return;
		}
	}

	public void simulate(List<SimulationInput> simInputs) throws SeMoDeException {

		// necessary for computing the output
		List<SimulationInputAndDistributionMap> values = new ArrayList<>();

		// interpret the load pattern and supply a distribution on a second basis
		LoadPatternInterpreter interpreter = new LoadPatternInterpreter(file);
		Map<Integer, Integer> loadDistributionPerSecond = interpreter.interpretLoadPattern();

		// adding the initial request distribution
		values.add(new SimulationInputAndDistributionMap("Initial distribution", loadDistributionPerSecond));

		for (SimulationInput simInput : simInputs) {

			LoadPatternSimulator sim = new LoadPatternSimulator(interpreter.getDoubleValues());
			Map<Integer, Integer> containerDistribution = sim.simulate(simInput);

			values.add(new SimulationInputAndDistributionMap(simInput.toString(), containerDistribution));
		}

		this.createSimulationDirectory();
		this.writeDistributionToFile("distribution.csv", values);

	}

	private void createSimulationDirectory() throws SeMoDeException {
		if (!Files.exists(Paths.get("simulation"))) {
			try {
				Files.createDirectory(Paths.get("simulation"));
			} catch (IOException e) {
				throw new SeMoDeException("Could not create simulation directory", e);
			}
		}

	}

	private void writeDistributionToFile(String fileName, List<SimulationInputAndDistributionMap> values) throws SeMoDeException {
		List<String> lines = new ArrayList<>();
		
		// find the maximum time in all the simulation steps
		Optional<Integer> max = values.stream()
			.map(s -> s.values.keySet().stream().max(Integer::compareTo))
			.filter(op -> op.isPresent())
			.map(op -> op.get())
			.max(Integer::compareTo);
		
		if(max.isPresent() == false) {
			throw new SeMoDeException("There is no value present in none of the provided maps");
		}
		
		int maxTime = max.get();
		String headline = "Timestamp;";
		for (SimulationInputAndDistributionMap simMap : values ) {
			headline += simMap.name + ";";
		}
		lines.add(headline);
		
		for ( int i = 0 ; i < maxTime ; i ++ ) {
			String line = "" + i + ";";
			
			for (SimulationInputAndDistributionMap simMap : values ) {
				if ( simMap.values.containsKey(i)) {
					line += simMap.values.get(i) + ";";
				} else {
					line += "0;";
				}
			}
				
			lines.add(line);
		}

		try {
			Files.write(Paths.get("simulation/" + fileName), lines);
		} catch (IOException e) {
			throw new SeMoDeException("Write distribution to file failed", e);
		}
	}

	private class SimulationInputAndDistributionMap {

		private final String name;
		private final Map<Integer, Integer> values;

		public SimulationInputAndDistributionMap(String name, Map<Integer, Integer> values) {
			super();
			this.name = name;
			this.values = values;
		}
	}
}
