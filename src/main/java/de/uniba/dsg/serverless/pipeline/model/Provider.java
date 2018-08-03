package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum Provider {

	AWS("aws"), AZURE("azure"), IBM("ibm"), GOOGLE("google");

	private String text;

	Provider(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	/**
	 * Returns the {@link Provider} associated to the given tag.
	 * 
	 * @param tag
	 * @return Provider
	 * @throws SeMoDeException
	 *             if the tag does not exist
	 */
	public static Provider fromString(String tag) throws SeMoDeException {
		for (Provider provider : Provider.values()) {
			if (provider.text.equalsIgnoreCase(tag)) {
				return provider;
			}
		}
		throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
	}

}
