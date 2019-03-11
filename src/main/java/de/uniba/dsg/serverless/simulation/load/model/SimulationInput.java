package de.uniba.dsg.serverless.simulation.load.model;

public class SimulationInput {

    private double averageExecutionTime;
    private double averageColdStartTime;
    private double shutdownAfter;
    
    public SimulationInput() {
    	
    }

    public SimulationInput(double averageExecutionTime, double averageColdStartTime, double shutdownAfter) {
        super();
        this.averageExecutionTime = averageExecutionTime;
        this.averageColdStartTime = averageColdStartTime;
        this.shutdownAfter = shutdownAfter;
    }

    public double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public double getAverageColdStartTime() {
        return averageColdStartTime;
    }

	public double getShutdownAfter() {
		return shutdownAfter;
	}

	@Override
	public String toString() {
		return "SimulationInput [" + averageExecutionTime + ","
				+ averageColdStartTime + "," + shutdownAfter + "]";
	}


}
