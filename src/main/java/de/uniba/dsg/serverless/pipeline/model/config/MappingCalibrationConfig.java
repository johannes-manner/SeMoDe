package de.uniba.dsg.serverless.pipeline.model.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Expose;
import de.uniba.dsg.serverless.util.SeMoDeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MappingCalibrationConfig {

    @Expose
    public String localCalibrationFile;
    @Expose
    public String providerCalibrationFile;
    @Expose
    public List<Integer> memorySizes;
    @Expose
    public Map<Integer, Double> memorySizeCPUShare;

    public MappingCalibrationConfig() {

    }

    /**
     * Copy constructor.
     *
     * @param mappingCalibrationConfig
     */
    public MappingCalibrationConfig(final MappingCalibrationConfig mappingCalibrationConfig) {
        this.providerCalibrationFile = mappingCalibrationConfig.providerCalibrationFile;
        this.localCalibrationFile = mappingCalibrationConfig.localCalibrationFile;
        this.memorySizes = List.copyOf(mappingCalibrationConfig.memorySizes);
        this.memorySizeCPUShare = Map.copyOf(mappingCalibrationConfig.memorySizeCPUShare);
    }

    public void update(final String localCalibrationFile, final String providerCalibrationFile, final String memoryJSON) throws SeMoDeException {
        if (!"".equals(localCalibrationFile)) {
            this.localCalibrationFile = localCalibrationFile;
        }
        if (!"".equals(providerCalibrationFile)) {
            this.providerCalibrationFile = providerCalibrationFile;
        }
        if (!"".equals(memoryJSON)) {
            try {
                this.memorySizes = (List<Integer>) new ObjectMapper().readValue(memoryJSON, ArrayList.class);
            } catch (final IOException e) {
                throw new SeMoDeException("Error during memory size parsing: " + memoryJSON);
            }
        }
    }
}
