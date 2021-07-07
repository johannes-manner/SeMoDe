package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * All transient fields are not included in the hashCode and equals.
 * Only when the object also changes in db the equals returns false.
 */
@Data
@Embeddable
public class MappingCalibrationConfig {

    @OneToOne
    private CalibrationConfig localCalibration;
    @Transient
    private Long localCalibrationId;

    @OneToOne
    private CalibrationConfig providerCalibration;
    @Transient
    private Long providerCalibrationId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        MappingCalibrationConfig that = (MappingCalibrationConfig) o;
        return this.localCalibration.equals(that.localCalibration) && this.providerCalibration.equals(that.providerCalibration) && this.memorySizesCalibration.equals(that.memorySizesCalibration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.localCalibration, this.providerCalibration, this.memorySizesCalibration);
    }

    @Override
    public String toString() {
        return "";
    }
}
