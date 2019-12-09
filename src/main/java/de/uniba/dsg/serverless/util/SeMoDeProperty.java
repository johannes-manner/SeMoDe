package de.uniba.dsg.serverless.util;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum SeMoDeProperty {
    // text is equivalent to the property name in the file config.properties
    BASH_LOCATION("bashExeLocation");

    private final String propertyName;

    private SeMoDeProperty(final String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Returns the {@link SeMoDeProperty} with the specified propertyName.
     *
     * @param propertyName
     * @return
     * @throws SeMoDeException if property with specified name does not exist.
     */
    public static SeMoDeProperty fromString(final String propertyName) throws SeMoDeException {
        for (final SeMoDeProperty property : SeMoDeProperty.values()) {
            if (property.getText().equalsIgnoreCase(propertyName)) {
                return property;
            }
        }
        throw new SeMoDeException("Property " + propertyName + " is not a valid SeMoDe Property");
    }

    public String getText() {
        return this.propertyName;
    }
}
