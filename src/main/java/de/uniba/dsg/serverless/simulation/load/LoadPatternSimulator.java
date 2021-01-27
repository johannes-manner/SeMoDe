package de.uniba.dsg.serverless.simulation.load;

import de.uniba.dsg.serverless.simulation.load.model.ContainerInstance;
import de.uniba.dsg.serverless.simulation.load.model.SimulationInput;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LoadPatternSimulator {

    private final List<Double> inputValues;

    public LoadPatternSimulator(final List<Double> inputValues) {
        this.inputValues = inputValues;
    }

    public Map<String, Map<Integer, Integer>> simulate(final SimulationInput simulationValues) {

        final SimulationStep simulation = new SimulationStep(simulationValues);

        for (final Double timestamp : this.inputValues) {

            // checks all running container, if they are finished, moving them to idle
            // containers
            simulation.checkFinishedContainers(timestamp);

            // checks all idle container, if they are longer idle than the assumed shutdown
            // period
            simulation.shutdownIdleContainer(timestamp);

            // check, if a container is idle
            final ContainerInstance container;
            if (simulation.idleContainerAvailable()) {
                // pick the first idle container and serve the request
                container = simulation.getArbitraryIdleContainer();
                container.executeRequest(timestamp, simulationValues);
            } else {
                // create a new container, add them to the simulation
                container = simulation.addNewContainer(timestamp);
                container.executeRequestWithColdStart(timestamp, simulationValues);
            }
            simulation.addToExecuting(container);

            log.info(container.toString());
        }

        // shutdown all containers (idle and running)
        simulation.shutdownAllContainer();

        // compute the distribution map
        final Map<Integer, Integer> containerDistribution = simulation.getContainerDistribution();
        // compute the output map
        final Map<Integer, Integer> outputLoadPattern = simulation.getOutputLoadPattern();

        final Map<String, Map<Integer, Integer>> simulationResults = new HashMap<>();
        simulationResults.put(simulationValues.toString(), containerDistribution);
        simulationResults.put(simulationValues.toString() + "-output load", outputLoadPattern);

        return simulationResults;
    }
}

class SimulationStep {

    private final List<ContainerInstance> containerList;
    private final List<ContainerInstance> executingContainers;
    private final List<ContainerInstance> idleContainers;
    private final SimulationInput simulationValues;
    private final Map<Integer, Integer> outputLoadPattern;

    public SimulationStep(final SimulationInput simulationValues) {
        this.containerList = new ArrayList<>();
        this.executingContainers = new ArrayList<>();
        this.idleContainers = new ArrayList<>();
        this.simulationValues = simulationValues;
        this.outputLoadPattern = new HashMap<>();
    }

    public boolean idleContainerAvailable() {
        return !this.idleContainers.isEmpty();
    }

    public ContainerInstance getArbitraryIdleContainer() {
        final ContainerInstance temp = this.idleContainers.get(0);
        this.idleContainers.remove(temp);
        return temp;
    }

    public ContainerInstance addNewContainer(final double ongoingTime) {
        final ContainerInstance temp = new ContainerInstance(ongoingTime);
        this.containerList.add(temp);
        return temp;
    }

    public void addToExecuting(final ContainerInstance container) {
        this.executingContainers.add(container);
    }

    /**
     * Checks if some executing containers are finished and move them to the idle
     * list. Also add them to the output load pattern map to see the incoming
     * request rate for another part of the application.
     */
    public void checkFinishedContainers(final double ongoingTime) {

        final List<ContainerInstance> finishedContainers = this.executingContainers.stream()
                .filter((c) -> c.getBussyUntil() < ongoingTime).collect(Collectors.toList());

        for (final ContainerInstance container : finishedContainers) {

            this.executingContainers.remove(container);
            this.idleContainers.add(container);

            // update the output load pattern list
            this.updateOutputLoadPatternList(container);
        }
    }

    /**
     * @param timestamp
     */
    public void shutdownIdleContainer(final double timestamp) {

        final double shutdownTime = this.simulationValues.getShutdownAfter();

        final List<ContainerInstance> containerForShutdown = this.idleContainers.stream()
                .filter((c) -> c.getBussyUntil() < (timestamp - shutdownTime)).collect(Collectors.toList());

        for (final ContainerInstance container : containerForShutdown) {
            container.setShutdownTime(container.getBussyUntil() + shutdownTime);
            this.idleContainers.remove(container);
        }
    }

    public void shutdownAllContainer() {

        final double shutdownTime = this.simulationValues.getShutdownAfter();

        for (final ContainerInstance container : this.executingContainers) {
            container.setShutdownTime(container.getBussyUntil() + shutdownTime);
            this.updateOutputLoadPatternList(container);
        }

        for (final ContainerInstance container : this.idleContainers) {
            container.setShutdownTime(container.getBussyUntil() + shutdownTime);
        }

    }

    public Map<Integer, Integer> getContainerDistribution() {

        final Map<Integer, Integer> containerDistribution = new HashMap<>();

        // max value for bussy until to get the size of the array (performance reasons)
        double maxBussyUntil = 0.0;
        for (final ContainerInstance container : this.containerList) {
            if (maxBussyUntil < container.getBussyUntil()) {
                maxBussyUntil = container.getBussyUntil();
            }
        }

        // 0 to 5.2 (maxBussyUntil) means 6 values in the array
        final int size = (int) maxBussyUntil + 1;
        final int[] containerDistributionInt = new int[size];

        for (int i = 0; i < size; i++) {
            containerDistributionInt[i] = 0;
        }

        for (final ContainerInstance container : this.containerList) {
            for (int i = (int) container.getStartTime(); i <= (int) container.getBussyUntil(); i++) {
                containerDistributionInt[i]++;
            }
        }

        for (int i = 0; i < size; i++) {
            containerDistribution.put(i, containerDistributionInt[i]);
        }

        return containerDistribution;
    }

    private void updateOutputLoadPatternList(final ContainerInstance container) {
        // uses the time until the container is bussy, which means that the container is
        // finished its execution
        final int finishTime = (int) container.getBussyUntil();
        if (this.outputLoadPattern.containsKey(finishTime)) {
            this.outputLoadPattern.put(finishTime, this.outputLoadPattern.get(finishTime) + 1);
        } else {
            this.outputLoadPattern.put(finishTime, 1);
        }
    }

    public Map<Integer, Integer> getOutputLoadPattern() {
        return this.outputLoadPattern;
    }
}
