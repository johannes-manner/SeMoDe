package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;

public enum CalibrationPlatform {

    LOCAL("local"), AWS("aws"), OPEN_FAAS("openfaas");

    private final String text;

    CalibrationPlatform(final String text) {
        this.text = text;
    }

    /**
     * Returns the {@link CalibrationPlatform} associated to the given tag.
     */
    public static CalibrationPlatform fromString(final String tag) throws SeMoDeException {
        for (final CalibrationPlatform mode : CalibrationPlatform.values()) {
            if (mode.text.equalsIgnoreCase(tag)) {
                return mode;
            }
        }
        throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
    }

    public String getText() {
        return this.text;
    }

}
