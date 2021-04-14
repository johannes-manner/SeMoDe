package de.uniba.dsg.serverless;

import de.uniba.dsg.serverless.cli.CustomUtility;
import de.uniba.dsg.serverless.cli.UtilityFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Run the application is a command line utility by specifying a that the app is not a web application. <br/>
 * <b>java -jar -Dspring.main.web-application-type=NONE app.jar</b>
 * </p>
 * <p>
 * Source: <a href="https://stackoverflow.com/questions/45615729/how-to-run-spring-boot-application-both-as-a-web-application-as-well-as-a-comman/45616851">Stack
 * Overflow Post</a>
 */
@Component
@ConditionalOnNotWebApplication
@Slf4j
public class ArgumentProcessor implements CommandLineRunner {

    private final UtilityFactory utilityFactory;

    @Autowired
    public ArgumentProcessor(UtilityFactory utilityFactory) {
        this.utilityFactory = utilityFactory;
    }

    @Override
    public void run(final String[] args) {

        if (args == null || args.length < 1) {
            log.warn("Please specify cli arguments. Program exited.");
            return;
        }

        final List<String> argumentList = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            argumentList.add(args[i]);
        }

        final Optional<CustomUtility> utility = this.utilityFactory.getUtilityClass(args[0]);
        if (utility.isPresent()) {
            utility.get().start(argumentList);
        } else {
            log.warn("The utility mechanism is not supported. Select a supported one");
        }
    }
}
