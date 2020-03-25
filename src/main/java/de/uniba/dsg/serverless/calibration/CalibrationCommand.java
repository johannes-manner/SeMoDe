package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.util.SeMoDeException;

public enum CalibrationCommand {

    PERFORM_CALIBRATION("calibrate"), MAPPING("mapping"), RUN_CONTAINER("runContainer");

    private final String text;

    CalibrationCommand(final String text) {
        this.text = text;
    }

    /**
     * Returns the {@link CalibrationCommand} associated to the given tag.
     *
     * @param tag
     * @return
     */
    public static CalibrationCommand fromString(final String tag) throws SeMoDeException {
        for (final CalibrationCommand command : CalibrationCommand.values()) {
            if (command.text.equalsIgnoreCase(tag)) {
                return command;
            }
        }
        throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
    }

    public String getText() {
        return this.text;
    }


}
