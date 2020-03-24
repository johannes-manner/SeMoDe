package de.uniba.dsg.serverless;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.cli.UtilityFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArgumentProcessor {

    public static void initLog4JParameters(final String[] args) {
        // defining dynamic properties for log4j2
        System.setProperty("CSV_SEPARATOR", ";");
        System.setProperty("DATE_TIME_FORMAT", "yyyy-MM-dd HH:mm:ss.SSS");
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tH:%1$tM:%1$tS:%1$tL] [%4$-7s] %5$s %n");
    }

    public static void main(final String[] args) {
        initLog4JParameters(args);

        final Logger logger = LogManager.getLogger(ArgumentProcessor.class.getName());

        if (args == null || args.length < 2) {
            logger.warn("Please specify cli arguments. Program exited.");
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
            logger.warn("The utility mechanism is not supported. Select a supported one");
        }
    }
}
