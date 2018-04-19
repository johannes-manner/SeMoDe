package de.uniba.dsg.serverless.util;

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
