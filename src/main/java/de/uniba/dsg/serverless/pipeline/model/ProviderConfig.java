package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.model.SeMoDeException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Model class for provider config and json serialization.
 * If you change the attribute list, also change the {@link ProviderConfig#jsonProviderProperties()} array.
 * DO NOT change this class. Otherwise json serialization and deserialization does not work properly.
 */
public class ProviderConfig {

    private String name;
    private List<Integer> memorySize;
    private List<String> language;
    private List<Integer> deploymentSize;

    public ProviderConfig() {
    }

    public static List<String> jsonProviderProperties() {
        return Arrays.asList("name", "memorySize", "language", "deploymentSize");
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

    public void validate(final Map<String, ProviderConfig> validConfigs) throws SeMoDeException {

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
}
