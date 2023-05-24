package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.simulation.load.SimulationUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UtilityFactory {

    private final List<CustomUtility> utilityList = new ArrayList<>();

    @Autowired
    public UtilityFactory(CliSetupService cliSetupService, CliHardwareCalibrationService cliHardwareCalibrationService, DeploymentSizeUtility deploymentSizeUtility) {
        this.utilityList.addAll(List.of(
                // parts of benchmarking
                cliSetupService,
                // hardware calibration
                cliHardwareCalibrationService,
                // file size enlarger for testing deployment hypotheses
                deploymentSizeUtility,

                // simulation
                // pre spring era
                new SimulationUtility("loadSimulation")
        ));
    }

    public Optional<CustomUtility> getUtilityClass(final String name) {
        return this.utilityList.stream().filter(c -> c.getName().equals(name)).findFirst();
    }
}
