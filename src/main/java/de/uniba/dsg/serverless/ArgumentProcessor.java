package de.uniba.dsg.serverless;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.cli.UtilityFactory;
import de.uniba.dsg.serverless.util.FileLogger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Run the application is a command line utility by specifying a that the app is not a web application. <br/>
 * <b>java -jar -Dspring.main.web-application-type=NONE app.jar</b>
 * </p>
 *
 * Source: <a href="https://stackoverflow.com/questions/45615729/how-to-run-spring-boot-application-both-as-a-web-application-as-well-as-a-comman/45616851">Stack
 * Overflow Post</a>
 */
@Component
@ConditionalOnNotWebApplication
public class ArgumentProcessor implements CommandLineRunner {

    // a global logger for the whole program
    public static FileLogger logger = new FileLogger("semode", "semode.log", true);

    @Override
    public void run(final String[] args) {
        // set logging system property
        // TODO think about property management
        System.setProperty("java.util.logging.SimpleFormatter.format", "[;%1$tFT%1$tH:%1$tM:%1$tS.%1$tL;] [%4$-7s] %5$s %n");
        System.setProperty("CSV_SEPARATOR", ";");

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
