package de.uniba.dsg.serverless.pipeline.calibration.local;

import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class PhysicalCoreFinder {

    /**
     * Returns the number of physical cores.<br>
     *
     * @return number of physical cores
     * @throws SeMoDeException when the command fails. (only supported on linux machines)
     * @see <a href="https://stackoverflow.com/questions/4759570/finding-number-of-cores-in-java">https://stackoverflow.com/questions/4759570/finding-number-of-cores-in-java</a>
     */
    public static int getPhysicalCores() throws SeMoDeException {

        final String command = getExecCommand();
        final Process process;
        try {
            if (isMac()) {
                String[] cmd = {"/bin/sh", "-c", command};
                process = Runtime.getRuntime().exec(cmd);
            } else {
                process = Runtime.getRuntime().exec(command);
            }
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }

        int numberOfCores = 0;
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isMac()) {
                    numberOfCores = line.length() > 0 ? Integer.parseInt(line) : 0;
                    log.info("Number of Cores: " + numberOfCores);
                    return numberOfCores;
                } else if (isUnix()) {
                    if (line.contains("Core(s) per socket:")) {
                        numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                        log.info("Number of Cores: " + numberOfCores);
                        return numberOfCores;
                    }
                } else if (isWindows()) {
                    if (line.contains("NumberOfCores")) {
                        numberOfCores = Integer.parseInt(line.split("=")[1]);
                        log.info("Number of Cores: " + numberOfCores);
                        return numberOfCores;
                    }
                }
            }
        } catch (final IOException e) {
            throw new SeMoDeException(e);
        }
        throw new SeMoDeException("Could not determine the number of cores. Check the OS support or contact the admin.");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0);
    }

    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    private static String getExecCommand() throws SeMoDeException {
        if (isWindows()) {
            return "cmd /C WMIC CPU Get /Format:List";
        } else if (isMac()) {
            return "sysctl -n machdep.cpu.core_count";
        } else if (isUnix()) {
            return "lscpu";
        } else {
            throw new SeMoDeException("Operating System is not supported. Only Windows and Linux are currently supported");
        }
    }
}
