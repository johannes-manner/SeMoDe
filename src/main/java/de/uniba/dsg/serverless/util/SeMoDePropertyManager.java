package de.uniba.dsg.serverless.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Global access point for all properties which are relevant for executing the SeMoDe Prototype.
 * The properties are stored in <i>src/main/resources/config.properties.</i>
 * Also look at {@link SeMoDeProperty} when adding properties.
 */
public final class SeMoDePropertyManager {

    private final static String PROP_FILE_NAME = "config.properties";
    private static SeMoDePropertyManager instance;

    private final Properties properties;

    private SeMoDePropertyManager() throws SeMoDeException {
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            this.properties = new Properties();
            this.properties.load(inputStream);
        } catch (final FileNotFoundException e) {
            throw new SeMoDeException("Global config properties file not found.", e);
        } catch (final IOException e) {
            throw new SeMoDeException("Global config file not readable", e);
        }
    }

    public static synchronized SeMoDePropertyManager getInstance() throws SeMoDeException {
        if (SeMoDePropertyManager.instance == null) {
            SeMoDePropertyManager.instance = new SeMoDePropertyManager();
        }
        return SeMoDePropertyManager.instance;
    }

    public String getProperty(final SeMoDeProperty property) {
        return this.properties.getProperty(property.getText());
    }
}
