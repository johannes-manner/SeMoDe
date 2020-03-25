package de.uniba.dsg.serverless;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.cli.UtilityFactory;
import de.uniba.dsg.serverless.util.FileLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArgumentProcessor {

    // a global logger for the whole program
    public static FileLogger logger;

    public static void main(final String[] args) {
        // set logging system property
        // TODO think about property management
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tH:%1$tM:%1$tS:%1$tL] [%4$-7s] %5$s %n");
        System.setProperty("CSV_SEPARATOR", ";");
        System.setProperty("DATE_TIME_FORMAT", "yyyy-MM-dd HH:mm:ss.SSS");

        logger = new FileLogger("semode", "semode.log", true);

        if (args == null || args.length < 2) {
            logger.warning("Please specify cli arguments. Program exited.");
            return;
        }

        final List<String> argumentList = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            argumentList.add(args[i]);
        }

        final Optional<CustomUtility> utility = UtilityFactory.getUtilityClass(args[0]);
        if (utility.isPresent()) {
            utility.get().start(argumentList);
        } else {
            logger.warning("The utility mechanism is not supported. Select a supported one");
        }
    }
}
