package de.uniba.dsg.serverless.simulation.load.model;

public class ContainerInstance {

	private static int internalIdCounter = 0;

	private final double startTime;
	private double shutdownTime;
	private final String id;

	private int servedRequests;
	private double bussyUntil;

	public ContainerInstance(double startTime) {
		super();
		this.id = "C-" + internalIdCounter++;
		this.startTime = startTime;
		this.servedRequests = 0;
		this.bussyUntil = 0.0;
	}

	public void executeRequest(double ongoingTime, SimulationInput simulationValues) {
		this.servedRequests++;
		this.bussyUntil = ongoingTime + simulationValues.getAverageExecutionTime();
	}

	public void executeRequestWithColdStart(double ongoingTime, SimulationInput simulationValues) {
		this.servedRequests++;
		this.bussyUntil = ongoingTime + simulationValues.getAverageExecutionTime()
				+ simulationValues.getAverageColdStartTime();
	}

	public double getStartTime() {
		return startTime;
	}

	public int getServedRequests() {
		return servedRequests;
	}

	public double getBussyUntil() {
		return bussyUntil;
	}

	public double getShutdownTime() {
		return shutdownTime;
	}

	public void setShutdownTime(double shutdownTime) {
		this.shutdownTime = shutdownTime;
	}

	@Override
	public String toString() {
		return "ContainerInstance [startTime=" + startTime + ", shutdownTime=" + shutdownTime + ", id=" + id
				+ ", servedRequests=" + servedRequests + ", bussyUntil=" + bussyUntil + "]";
	}
}
