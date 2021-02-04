package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@Embeddable
public class MappingCalibrationConfig {

    private String localCalibrationFile = "";
    private String providerCalibrationFile = "";
    private String memorySizesCalibration = "";
    @Transient
    private Map<Integer, Double> memorySizeCPUShare;

    public List<Integer> getMemorySizeList() {
        if (this.memorySizesCalibration == null) {
            return List.of();
        }
        return Arrays.stream(this.memorySizesCalibration.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

}
