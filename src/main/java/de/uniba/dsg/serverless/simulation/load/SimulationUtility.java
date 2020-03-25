package de.uniba.dsg.serverless.simulation.load;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.dsg.serverless.ArgumentProcessor;
import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.simulation.load.model.SimulationInput;
import de.uniba.dsg.serverless.util.FileLogger;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulationUtility extends CustomUtility {

    private static final FileLogger logger = ArgumentProcessor.logger;

    private Path file;

    public SimulationUtility(final String name) {
        super(name);
    }

    @Override
    public void start(final List<String> args) {

        if (args == null || args.size() != 2) {
            logger.warning("The provided element size is not correct! " + args);
            return;
        }

        try {
            this.file = Paths.get(args.get(0));
            final List<SimulationInput> simulationInput = new ObjectMapper().readValue(args.get(1),
                    new TypeReference<List<SimulationInput>>() {
                    });
            this.simulate(simulationInput);

        } catch (final IOException e) {
            e.printStackTrace();
            logger.warning("Error reading simulation input json! " + args.get(1));
            return;
        } catch (final SeMoDeException e) {
            logger.warning(e.getMessage());
            return;
        }
    }

    public void simulate(final List<SimulationInput> simInputs) throws SeMoDeException {

        // necessary for computing the output
        final List<SimulationInputAndDistributionMap> values = new ArrayList<>();

        // interpret the load pattern and supply a distribution on a second basis
        final LoadPatternInterpreter interpreter = new LoadPatternInterpreter(this.file);
        final Map<Integer, Integer> loadDistributionPerSecond = interpreter.interpretLoadPattern();

        // adding the initial request distribution
        values.add(new SimulationInputAndDistributionMap("Initial distribution", loadDistributionPerSecond));

        for (final SimulationInput simInput : simInputs) {

            final LoadPatternSimulator sim = new LoadPatternSimulator(interpreter.getDoubleValues());
            final Map<String, Map<Integer, Integer>> simulationResults = sim.simulate(simInput);

            for (final String key : simulationResults.keySet()) {
                values.add(new SimulationInputAndDistributionMap(key, simulationResults.get(key)));
            }
        }

        this.createSimulationDirectory();
        this.writeDistributionToFile("distribution.csv", values);

    }

    private void createSimulationDirectory() throws SeMoDeException {
        if (!Files.exists(Paths.get("simulation"))) {
            try {
                Files.createDirectory(Paths.get("simulation"));
            } catch (final IOException e) {
                throw new SeMoDeException("Could not create simulation directory", e);
            }
        }

    }

    private void writeDistributionToFile(final String fileName, final List<SimulationInputAndDistributionMap> values)
            throws SeMoDeException {
        final List<String> lines = new ArrayList<>();

        // find the maximum time in all the simulation steps
        final Optional<Integer> max = values.stream().map(s -> s.values.keySet().stream().max(Integer::compareTo))
                .filter(op -> op.isPresent()).map(op -> op.get()).max(Integer::compareTo);

        if (max.isPresent() == false) {
            throw new SeMoDeException("There is no value present in none of the provided maps");
        }

        final int maxTime = max.get();
        String headline = "Timestamp;";
        for (final SimulationInputAndDistributionMap simMap : values) {
            headline += simMap.name + ";";
        }
        lines.add(headline);

        for (int i = 0; i <= maxTime; i++) {
            String line = "" + i + ";";

            for (final SimulationInputAndDistributionMap simMap : values) {
                if (simMap.values.containsKey(i)) {
                    line += simMap.values.get(i) + ";";
                } else {
                    line += "0;";
                }
            }

            lines.add(line);
        }

        try {
            Files.write(Paths.get("simulation/" + fileName), lines);
        } catch (final IOException e) {
            throw new SeMoDeException("Write distribution to file failed", e);
        }
    }

    private class SimulationInputAndDistributionMap {

        private final String name;
        private final Map<Integer, Integer> values;

        public SimulationInputAndDistributionMap(final String name, final Map<Integer, Integer> values) {
            super();
            this.name = name;
            this.values = values;
        }
    }
}
