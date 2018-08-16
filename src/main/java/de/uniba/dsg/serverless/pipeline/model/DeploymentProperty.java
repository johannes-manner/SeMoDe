package de.uniba.dsg.serverless.pipeline.model;

import java.util.List;
import java.util.Objects;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class DeploymentProperty<T> {

	public final String key;
	public final List<T> possibleValues;

	private List<T> values;

	public final boolean valuesRestricted;

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
	public DeploymentProperty(String key, List<T> possibleValues) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(possibleValues);
		this.key = key;
		this.possibleValues = possibleValues;
		valuesRestricted = true;
	}

	/**
	 * Creates a new Deployment Property which specifies one dimension of the
	 * deployment package.
	 * 
	 * @param key
	 *            name in the properties file
	 */
	public DeploymentProperty(String key) {
		Objects.requireNonNull(key);
		this.key = key;
		this.possibleValues = null;
		valuesRestricted = false;
	}

	public List<T> getValues() {
		return values;
	}

	public void setValues(List<T> values) throws SeMoDeException {
		if (valuesRestricted) {
			for (T v : values) {
				if (!possibleValues.contains(v)) {
					throw new SeMoDeException("Value " + v + "is not permitted / implemented.");
				}
			}
		}
		this.values = values;
	}

}
