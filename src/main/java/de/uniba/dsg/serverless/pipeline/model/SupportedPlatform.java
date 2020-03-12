package de.uniba.dsg.serverless.pipeline.model;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum SupportedPlatform {

    LOCAL("local"), AWS("aws");

    private final String text;

    SupportedPlatform(final String text) {
        this.text = text;
    }

    /**
     * Returns the {@link SupportedPlatform} associated to the given tag.
     *
     * @param tag
     * @return
     */
    public static SupportedPlatform fromString(final String tag) throws SeMoDeException {
        for (final SupportedPlatform mode : SupportedPlatform.values()) {
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
