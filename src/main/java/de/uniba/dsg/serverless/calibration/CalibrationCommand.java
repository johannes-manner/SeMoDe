package de.uniba.dsg.serverless.calibration;

import de.uniba.dsg.serverless.model.SeMoDeException;

public enum CalibrationCommand {

    PERFORM_CALIBRATION("calibrate"), INFO("info"), RUN_CONTAINER("runContainer");

    private String text;

    CalibrationCommand(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    /**
     * Returns the {@link CalibrationCommand} associated to the given tag.
     *
     * @param tag
     * @return
     */
    public static CalibrationCommand fromString(String tag) throws SeMoDeException {
        for (CalibrationCommand command : CalibrationCommand.values()) {
            if (command.text.equalsIgnoreCase(tag)) {
                return command;
            }
        }
        throw new SeMoDeException("Mode is unknown. Entered mode = " + tag);
    }


}
