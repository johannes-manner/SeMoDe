package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum CalibrationPlatform {

    LOCAL("local"), AWS("aws");

    private String text;

    CalibrationPlatform(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    /**
     * Returns the {@link CalibrationPlatform} associated to the given tag.
     *
     * @param tag
     * @return
     */
    public static CalibrationPlatform fromString(String tag) throws SeMoDeException {
        for (CalibrationPlatform mode : CalibrationPlatform.values()) {
            if (mode.text.equalsIgnoreCase(tag)) {
                return mode;
            }
        }
        throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
    }

}
