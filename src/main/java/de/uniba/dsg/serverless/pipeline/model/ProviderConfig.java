package de.uniba.dsg.serverless.pipeline.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.dsg.serverless.model.SeMoDeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model class for provider config and json serialization.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class ProviderConfig {

    private String name;
    private List<Integer> memorySize;
    private List<String> language;
    private List<Integer> deploymentSize;

    public ProviderConfig() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Integer> getMemorySize() {
        return this.memorySize;
    }

    public void setMemorySize(final List<Integer> memorySize) {
        this.memorySize = memorySize;
    }

    public List<String> getLanguage() {
        return this.language;
    }

    public void setLanguage(final List<String> language) {
        this.language = language;
    }

    public List<Integer> getDeploymentSize() {
        return this.deploymentSize;
    }

    public void setDeploymentSize(final List<Integer> deploymentSize) {
        this.deploymentSize = deploymentSize;
    }

    @Override
    public String toString() {
        return "ProviderConfig [name=" + this.name + ", memorySize=" + this.memorySize + ", language=" + this.language
                + ", deploymentSize=" + this.deploymentSize + "]";
    }

    private void validate(final Map<String, ProviderConfig> validConfigs) throws SeMoDeException {

        if (!validConfigs.containsKey(this.getName())) {
            throw new SeMoDeException("The provider name is not included in the valid configurations :" + this.getName());
        }

        final ProviderConfig validConfig = validConfigs.get(this.getName());
        if (!this.getName().equals(validConfig.getName())) {
            throw new SeMoDeException("The name property is not valid for the given provider config");
        }
        for (final String language : this.getLanguage()) {
            if (!validConfig.getLanguage().contains(language)) {
                throw new SeMoDeException("The language property is not valid for the given provider config: " + language);
            }
        }
        for (final Integer memorySize : this.getMemorySize()) {
            if (!validConfig.getMemorySize().contains(memorySize)) {
                throw new SeMoDeException("The memorySize property is not valid for the given provider config: " + memorySize);
            }
        }
    }

    public void validateAndCreate(final Map<String, ProviderConfig> providerConfigMap, final String provider, final String memorySize, final String language, final String deploymentSize) throws SeMoDeException, IOException {
        // update
        if (!"".equals(this.name)) this.name = provider;
        if (!"".equals(memorySize))
            this.memorySize = (List<Integer>) new ObjectMapper().readValue(memorySize, ArrayList.class);
        if (!"".equals(language))
            this.language = (List<String>) new ObjectMapper().readValue(language, ArrayList.class);
        if (!"".equals(deploymentSize))
            this.deploymentSize = (List<Integer>) new ObjectMapper().readValue(deploymentSize, ArrayList.class);

        // validate
        this.validate(providerConfigMap);
    }

    public void validateAndUpdate(final Map<String, ProviderConfig> providerConfigMap, final String memorySize, final String language, final String deploymentSize) throws SeMoDeException, IOException {
        // update
        if (!"".equals(memorySize))
            this.memorySize = (List<Integer>) new ObjectMapper().readValue(memorySize, ArrayList.class);
        if (!"".equals(language))
            this.language = (List<String>) new ObjectMapper().readValue(language, ArrayList.class);
        if (!"".equals(deploymentSize))
            this.deploymentSize = (List<Integer>) new ObjectMapper().readValue(deploymentSize, ArrayList.class);

        // validate
        this.validate(providerConfigMap);
    }
}
