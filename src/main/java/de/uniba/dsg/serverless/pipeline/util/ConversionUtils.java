package de.uniba.dsg.serverless.pipeline.util;

import de.uniba.dsg.serverless.pipeline.calibration.model.CalibrationEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversionUtils {

    public Map<Double, List<Double>> mapCalibrationEventList(List<CalibrationEvent> calibrationEvents) {
        Map<Double, List<Double>> memoryOrCPUAndItsMeasures = new HashMap<>();
        for (CalibrationEvent event : calibrationEvents) {
            if (!memoryOrCPUAndItsMeasures.containsKey(event.getCpuOrMemoryQuota())) {
                memoryOrCPUAndItsMeasures.put(event.getCpuOrMemoryQuota(), new ArrayList<>());
            }
            memoryOrCPUAndItsMeasures.get(event.getCpuOrMemoryQuota()).add(event.getGflops());
        }
        return memoryOrCPUAndItsMeasures;
    }

}
