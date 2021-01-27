package de.uniba.dsg.serverless.pipeline.model.config;

import com.google.gson.annotations.Expose;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
public class MappingCalibrationConfig {

    @Expose
    public String localCalibrationFile;
    @Expose
    public String providerCalibrationFile;
    @Expose
    public String memorySizes;
    @Expose
    public Map<Integer, Double> memorySizeCPUShare;

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

    // TODO remove
    public void update(final String localCalibrationFile, final String providerCalibrationFile, final String memoryJSON) throws SeMoDeException {
        if (!"".equals(localCalibrationFile)) {
            this.localCalibrationFile = localCalibrationFile;
        }
        if (!"".equals(providerCalibrationFile)) {
            this.providerCalibrationFile = providerCalibrationFile;
        }
//        if (!"".equals(memoryJSON)) {
//            try {
//                this.memorySizes = (List<Integer>) new ObjectMapper().readValue(memoryJSON, ArrayList.class);
//            } catch (final IOException e) {
//                throw new SeMoDeException("Error during memory size parsing: " + memoryJSON);
//            }
//        }
    }
}
