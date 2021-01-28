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

    private String localCalibrationFile;
    private String providerCalibrationFile;
    private String memorySizes;
    @Transient
    private Map<Integer, Double> memorySizeCPUShare;

    public MappingCalibrationConfig() {

    }

    public List<Integer> getMemorySizeList() {
        if (this.memorySizes == null) {
            return List.of();
        }
        return Arrays.stream(this.memorySizes.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

}
