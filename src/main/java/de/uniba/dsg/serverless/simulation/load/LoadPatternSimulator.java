package de.uniba.dsg.serverless.simulation.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uniba.dsg.serverless.simulation.load.model.ContainerInstance;
import de.uniba.dsg.serverless.simulation.load.model.SimulationInput;

public class LoadPatternSimulator {
	
	private static final Logger logger = LogManager.getLogger(LoadPatternSimulator.class);

	private final List<Double> inputValues;

	public LoadPatternSimulator(List<Double> inputValues) {
		this.inputValues = inputValues;
	}

	public Map<Integer, Integer> simulate(SimulationInput simulationValues) {

		SimulationStep simulation = new SimulationStep(simulationValues);

		for (Double timestamp : inputValues) {

			// checks all running container, if they are finished, moving them to idle containers
			simulation.checkFinishedContainers(timestamp);

			// checks all idle container, if they are longer idle than the assumed shutdown period
			simulation.shutdownIdleContainer(timestamp);

			// check, if a container is idle
			ContainerInstance container;
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

			logger.info(container);
		}

		// shutdown all containers (idle and running)
		simulation.shutdownAllContainer();

		// compute the distribution map
		return simulation.getContainerDistribution();
	}
}

class SimulationStep {

	private final List<ContainerInstance> containerList;
	private final List<ContainerInstance> executingContainers;
	private final List<ContainerInstance> idleContainers;
	private final SimulationInput simulationValues;

	public SimulationStep(SimulationInput simulationValues) {
		this.containerList = new ArrayList<>();
		this.executingContainers = new ArrayList<>();
		this.idleContainers = new ArrayList<>();
		this.simulationValues = simulationValues;
	}

	public boolean idleContainerAvailable() {
		return !idleContainers.isEmpty();
	}

	public ContainerInstance getArbitraryIdleContainer() {
		ContainerInstance temp = idleContainers.get(0);
		this.idleContainers.remove(temp);
		return temp;
	}

	public ContainerInstance addNewContainer(double ongoingTime) {
		ContainerInstance temp = new ContainerInstance(ongoingTime);
		this.containerList.add(temp);
		return temp;
	}

	public void addToExecuting(ContainerInstance container) {
		this.executingContainers.add(container);
	}

	/**
	 * Checks if some executing containers are finished and move them to the idle
	 * list.
	 */
	public void checkFinishedContainers(double ongoingTime) {

		List<ContainerInstance> finishedContainers = this.executingContainers.stream()
				.filter((c) -> c.getBussyUntil() < ongoingTime).collect(Collectors.toList());

		for (ContainerInstance container : finishedContainers) {

			this.executingContainers.remove(container);
			this.idleContainers.add(container);

		}
	}

	/**
	 * @param timestamp
	 */
	public void shutdownIdleContainer(double timestamp) {

		double shutdownTime = this.simulationValues.getShutdownAfter();

		List<ContainerInstance> containerForShutdown = this.idleContainers.stream()
				.filter((c) -> c.getBussyUntil() < (timestamp - shutdownTime)).collect(Collectors.toList());

		for (ContainerInstance container : containerForShutdown) {
			container.setShutdownTime(container.getBussyUntil() + shutdownTime);
			this.idleContainers.remove(container);
		}
	}

	public void shutdownAllContainer() {

		double shutdownTime = this.simulationValues.getShutdownAfter();

		for (ContainerInstance container : this.executingContainers) {
			container.setShutdownTime(container.getBussyUntil() + shutdownTime);
		}

		for (ContainerInstance container : this.idleContainers) {
			container.setShutdownTime(container.getBussyUntil() + shutdownTime);
		}

	}

	public Map<Integer, Integer> getContainerDistribution() {

		Map<Integer, Integer> containerDistribution = new HashMap<>();

		// max value for bussy until to get the size of the array (performance reasons)
		double maxBussyUntil = 0.0;
		for (ContainerInstance container : this.containerList) {
			if (maxBussyUntil < container.getBussyUntil()) {
				maxBussyUntil = container.getBussyUntil();
			}
		}

		// 0 to 5.2 (maxBussyUntil) means 6 values in the array
		int size = (int) maxBussyUntil + 1;
		int[] containerDistributionInt = new int[size];
		
		for (int i = 0; i < size; i++) {
			containerDistributionInt[i] = 0;
		}
		
		for (ContainerInstance container : this.containerList) {
			for (int i = (int) container.getStartTime(); i <= (int) container.getBussyUntil(); i++) {
				containerDistributionInt[i]++;
			}
		}

		for (int i = 0; i < size; i++) {
			containerDistribution.put(i, containerDistributionInt[i]);
		}
		
		return containerDistribution;
	}
}
