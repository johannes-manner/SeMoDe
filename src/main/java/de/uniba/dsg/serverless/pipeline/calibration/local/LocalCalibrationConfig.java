package de.uniba.dsg.serverless.pipeline.calibration.local;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import lombok.Data;

// TODO add bean validation
@Data
public class LocalCalibrationConfig {

    private double localSteps;
    private int numberOfLocalCalibrations;
    private boolean localEnabled;
    private String dockerSourceFolder;

    public LocalCalibrationConfig() {
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
}
