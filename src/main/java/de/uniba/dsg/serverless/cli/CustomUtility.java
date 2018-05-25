package de.uniba.dsg.serverless.cli;

import java.util.List;

public abstract class CustomUtility {

	private String name;
	
	public CustomUtility(String name) {
		this.name = name;
	}
	
	public abstract void start(List<String> args);
	
	public String getName() {
		return this.name;
	}
}
