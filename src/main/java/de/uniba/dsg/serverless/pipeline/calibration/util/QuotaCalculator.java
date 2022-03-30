package de.uniba.dsg.serverless.pipeline.calibration.util;

import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class QuotaCalculator {

    public static List<Double> calculateQuotas(double localSteps) throws SeMoDeException {
        final int physicalCores = PhysicalCoreFinder.getPhysicalCores();
        log.info("Number of cores: " + physicalCores);
        return IntStream
                // 1.1 results from 1 + avoid rounding errors (0.1)
                .range(1, (int) (1.1 + ((double) physicalCores * 1.0 / localSteps)))
                .mapToDouble(v -> localSteps * v)
                .boxed()
                .collect(Collectors.toList());
    }
}
