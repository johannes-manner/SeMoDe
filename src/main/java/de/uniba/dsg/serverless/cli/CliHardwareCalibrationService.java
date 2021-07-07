package de.uniba.dsg.serverless.cli;

import de.uniba.dsg.serverless.pipeline.calibration.local.LocalCalibration;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.RegressionComputation;
import de.uniba.dsg.serverless.pipeline.calibration.mapping.SimpleFunction;
import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import de.uniba.dsg.serverless.pipeline.model.config.LocalCalibrationConfig;
import de.uniba.dsg.serverless.pipeline.util.ConversionUtils;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CliHardwareCalibrationService implements CustomUtility {

    private final ConversionUtils conversionUtils;

    @Value("${semode.hardware.steps}")
    private double steps;
    @Value("${semode.hardware.runs}")
    private int runs;
    @Value("${semode.hardware.image}")
    private String image;


    @Autowired
    public CliHardwareCalibrationService(ConversionUtils conversionUtils) {
        this.conversionUtils = conversionUtils;
    }

    @Override
    public void start(List<String> args) {
        try {
            log.info("Hardware calibration. Runs: " + this.runs + " Steps: " + this.steps + " This may take a while...");
            LocalCalibrationConfig c = new LocalCalibrationConfig();
            c.setLocalSteps(this.steps);
            c.setNumberOfLocalCalibrations(this.runs);
            LocalCalibration lC = new LocalCalibration("hardware", c);

            List<CalibrationEvent> calibrationEvents = lC.startCliHardwareCalibration(this.image);
            Map<Double, List<Double>> quotaGflops = this.conversionUtils.mapCalibrationEventList(calibrationEvents);
            SimpleFunction regression = RegressionComputation.computeRegression(quotaGflops);
            log.info(regression.toString());

        } catch (SeMoDeException e) {
            log.warn("Error in local calibration pipeline: " + e.getMessage());
        }

    }

    @Override
    public String getName() {
        return "hardwareCalibration";
    }
}
