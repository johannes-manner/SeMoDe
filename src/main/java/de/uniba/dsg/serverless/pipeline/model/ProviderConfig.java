package de.uniba.dsg.serverless.pipeline.model;

import java.util.Arrays;
import java.util.List;

/**
 * If you change the attribute list, also change the {@link ProviderConfig#jsonProviderProperties()} array.
 */
public class ProviderConfig {

	private String name;
	private List<Integer> memorySize;
	private List<String> language;

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

	@Override
	public String toString() {
		return "Provider [name=" + name + ", memorySize=" + memorySize + ", language=" + language + "]";
	}

	public static List<String> jsonProviderProperties(){
		return Arrays.asList("name", "memorySize", "language");
	}
}
