package de.uniba.dsg.serverless.pipeline.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class DeploymentProperty {

	public final String key;
	public List<String> possibleValues;

	private List<String> values;

	public final Class<?> propertyClass;

	/**
	 * Creates a new Deployment Property which specifies one dimension of the
	 * deployment package.
	 * 
	 * @param key
	 *            name in the properties file
	 * @param possibleValues
	 *            restricts the value assignment to the list of possible
	 *            assignments.<br>
	 *            Use the constructor DeploymentProperty(key) if this is not needed.
	 */
	public DeploymentProperty(String key, Class<?> propertyClass, List<String> possibleValues) throws SeMoDeException {
		this(key, propertyClass);
		if (possibleValues == null) {
			throw new SeMoDeException("Possible values must not be null.");
		}
		this.possibleValues = possibleValues;
	}

	/**
	 * Creates a new Deployment Property which specifies one dimension of the
	 * deployment package.
	 * 
	 * @param key
	 *            name in the properties file
	 */
	public DeploymentProperty(String key, Class<?> propertyClass) throws SeMoDeException {
		if (key == null) {
			throw new SeMoDeException("Key must not be null.");
		}
		this.key = key;
		if (!propertyClass.equals(Integer.class) && !propertyClass.equals(String.class)) {
			throw new SeMoDeException("propertyClass does not match one of the defined Classes.");
		}
		this.propertyClass = propertyClass;
		possibleValues = new ArrayList<>();
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) throws SeMoDeException {
		for (String v : values) {
			if (propertyClass.equals(Integer.class) && !v.matches("\\d+")) {
				throw new SeMoDeException("Value must be a number.");
			}
			if (valuesRestricted() && !possibleValues.contains(v)) {
				throw new SeMoDeException("Value " + v + " is not permitted / implemented.");
			}
		}
		this.values = values;
	}

	public void setRawValues(String rawProperty) throws SeMoDeException {
		List<String> propertyString = Arrays.asList(rawProperty.split(BenchmarkSetup.SEPERATOR));
		setValues(propertyString);
	}

	private boolean valuesRestricted() {
		return !possibleValues.isEmpty();
	}

	public List<Integer> getIntValues() throws SeMoDeException {
		if (!propertyClass.equals(Integer.class)) {
			throw new SeMoDeException("not available");
		}
		return values.stream().map(Integer::parseInt).collect(Collectors.toList());
	}

}
