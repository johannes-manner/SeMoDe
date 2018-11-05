package de.uniba.dsg.serverless.pipeline.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.uniba.dsg.serverless.model.SeMoDeException;

/**
 * If you change the attribute list, also change the {@link ProviderConfig#jsonProviderProperties()} array.
 */
public class ProviderConfig {

	private String name;
	private List<Integer> memorySize;
	private List<String> language;
	private List<Integer> deploymentSize;

	public ProviderConfig() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(List<Integer> memorySize) {
		this.memorySize = memorySize;
	}

	public List<String> getLanguage() {
		return language;
	}

	public void setLanguage(List<String> language) {
		this.language = language;
	}

	public List<Integer> getDeploymentSize() {
		return deploymentSize;
	}

	public void setDeploymentSize(List<Integer> deploymentSize) {
		this.deploymentSize = deploymentSize;
	}

	@Override
	public String toString() {
		return "ProviderConfig [name=" + name + ", memorySize=" + memorySize + ", language=" + language
				+ ", deploymentSize=" + deploymentSize + "]";
	}

	public static List<String> jsonProviderProperties(){
		return Arrays.asList("name", "memorySize", "language", "deploymentSize");
	}
	
	public void validate(Map<String, ProviderConfig> validConfigs) throws SeMoDeException {
		
		if(!validConfigs.containsKey(this.getName())) {
			throw new SeMoDeException("The provider name is not included in the valid configurations :" + this.getName());
		}
		
		ProviderConfig validConfig = validConfigs.get(this.getName());
		if(!this.getName().equals(validConfig.getName())) {
			throw new SeMoDeException("The name property is not valid for the given provider config");
		}
		for(String language : this.getLanguage()) {
			if(!validConfig.getLanguage().contains(language)) {
				throw new SeMoDeException("The language property is not valid for the given provider config: " + language);
			}
		}
		for(Integer memorySize : this.getMemorySize()) {
			if(!validConfig.getMemorySize().contains(memorySize)) {
				throw new SeMoDeException("The memorySize property is not valid for the given provider config: " + memorySize);
			}
		}
	}
}
