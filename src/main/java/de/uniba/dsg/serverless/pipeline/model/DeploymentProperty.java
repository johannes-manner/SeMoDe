package de.uniba.dsg.serverless.pipeline.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class DeploymentProperty {

	public final String key;
	public List<?> possibleValues;

	private List<?> values;

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
	public DeploymentProperty(String key, Class<?> propertyClass, List<?> possibleValues) throws SeMoDeException {
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

	public List<?> getValues() {
		return values;
	}

	public void setValues(List<?> values) throws SeMoDeException {
		for (Object v : values) {
			if (!v.getClass().equals(propertyClass)) {
				throw new SeMoDeException("Value " + v + " is not of defined type " + propertyClass.getName());
			}
			if (valuesRestricted() && !possibleValues.contains(v)) {
				throw new SeMoDeException("Value " + v + " is not permitted / implemented.");
			}
		}
		this.values = values;
	}

	public void setRawValues(String rawProperty) throws SeMoDeException {
		List<String> propertyString = Arrays.asList(rawProperty.split(BenchmarkSetup.SEPERATOR));
		if (propertyClass.equals(String.class)) {
			setValues(propertyString);
		} else if (propertyClass.equals(Integer.class)) {
			try {
				setValues(propertyString.stream().map(Integer::parseInt).collect(Collectors.toList()));
			} catch (NumberFormatException e) {
				throw new SeMoDeException(e);
			}
		}
	}

	private boolean valuesRestricted() {
		return !possibleValues.isEmpty();
	}

}
