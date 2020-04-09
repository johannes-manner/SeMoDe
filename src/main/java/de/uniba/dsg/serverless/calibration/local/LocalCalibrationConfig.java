package de.uniba.dsg.serverless.calibration.local;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public class LocalCalibrationConfig {

    private double localSteps;
    private int numberOfLocalCalibrations;
    private boolean localEnabled;
    private String dockerSourceFolder;

    public LocalCalibrationConfig() {
    }

    public LocalCalibrationConfig(final double localSteps, final int numberOfLocalCalibrations, final boolean localEnabled, final String dockerSourceFolder) {
        this.localSteps = localSteps;
        this.numberOfLocalCalibrations = numberOfLocalCalibrations;
        this.localEnabled = localEnabled;
        this.dockerSourceFolder = dockerSourceFolder;
    }

    public LocalCalibrationConfig(final LocalCalibrationConfig localCalibrationConfig) {
        this.localSteps = localCalibrationConfig.getLocalSteps();
        this.numberOfLocalCalibrations = localCalibrationConfig.getNumberOfLocalCalibrations();
        this.localEnabled = localCalibrationConfig.localEnabled;
        this.dockerSourceFolder = localCalibrationConfig.dockerSourceFolder;
    }

    public double getLocalSteps() {
        return this.localSteps;
    }

    public void setLocalSteps(final double localSteps) {
        this.localSteps = localSteps;
    }

    public int getNumberOfLocalCalibrations() {
        return this.numberOfLocalCalibrations;
    }

    public void setNumberOfLocalCalibrations(final int numberOfLocalCalibrations) {
        this.numberOfLocalCalibrations = numberOfLocalCalibrations;
    }

    public boolean isLocalEnabled() {
        return this.localEnabled;
    }

    public void setLocalEnabled(final boolean localEnabled) {
        this.localEnabled = localEnabled;
    }

    public String getDockerSourceFolder() {
        return this.dockerSourceFolder;
    }

    public void setDockerSourceFolder(final String dockerSourceFolder) {
        this.dockerSourceFolder = dockerSourceFolder;
    }

    public void update(final String steps, final String numberOfLocalCalibrations, final String enabled, final String dockerSourceFolder) {
        if (!"".equals(steps) && Doubles.tryParse(steps) != null) {
            this.localSteps = Doubles.tryParse(steps);
        }
        if (!"".equals(numberOfLocalCalibrations) && Ints.tryParse(numberOfLocalCalibrations) != null) {
            this.numberOfLocalCalibrations = Ints.tryParse(numberOfLocalCalibrations);
        }
        if (!"".equals(enabled)) {
            // returns only true, if enabled is "true", otherwise false (also for incorrect inputs or null)
            this.localEnabled = Boolean.parseBoolean(enabled);
        }
        if (!"".equals(dockerSourceFolder)) {
            this.dockerSourceFolder = dockerSourceFolder.trim();
        }
    }

    @Override
    public String toString() {
        return "LocalCalibrationConfig{" +
                "localSteps=" + this.localSteps +
                ", numberOfLocalCalibrations=" + this.numberOfLocalCalibrations +
                ", localEnabled=" + this.localEnabled +
                ", dockerSourceFolder='" + this.dockerSourceFolder + '\'' +
                '}';
    }
}
